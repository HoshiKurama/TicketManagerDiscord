package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.ticketmanager.api.events.*
import com.github.hoshikurama.ticketmanager.api.impl.TicketManager
import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.mode.RelayMode
import com.github.hoshikurama.tmdiscord.setup.mode.ClientMode
import com.github.hoshikurama.tmdiscord.setup.mode.Mode
import com.github.hoshikurama.tmdiscord.setup.mode.instance
import com.github.hoshikurama.tmdiscord.utility.convertEventToNotification
import com.github.hoshikurama.tmdiscord.utility.unwrapOrReturn
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

@Suppress("unused")
class BukkitPlatform : JavaPlugin(), Listener, CommandExecutor {
    private val eventSubscribers = mutableSetOf<() -> Unit>()
    private var client: ClientMode? = null
    private var relay: RelayMode? = null

    private var clientModeUnregisterListener: (() -> Unit)? = null
    private var relayModeUnregisterListener: (() -> Unit)? = null


    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val kordThread = newSingleThreadContext("TMDiscordBot_Kord")
    private val kordScope = CoroutineScope(kordThread)

    private inline fun runOnMainThread(crossinline f: () -> Unit) {
        Bukkit.getScheduler().runTask(this, Runnable { f() })
    }

    private suspend fun setupClientMode(commonConfig: CommonConfig, primaryDataFolder: Path) {
        val clientResult = ClientMode.instance(
            commonConfig = commonConfig,
            dataFolder = primaryDataFolder,
            sharedPlatform = logger::severe
        )

        if (clientResult.isSuccess) {
            client = clientResult.getOrThrow()
            kordScope.launch { client?.kordBot?.login() }
        } else {
            logger.severe("An unrecoverable error occurred!")
            clientResult.exceptionOrNull()?.printStackTrace()
        }

        // Subscribe to event
        clientModeUnregisterListener = TicketManager.EventBus.subscribe<TicketEvent.WithAction> { event ->
            if (event is TicketEvent.CanBeSilent && event.wasSilent) return@subscribe

            val clientNow = client ?: return@subscribe
            val notification = convertEventToNotification(event, clientNow.locale)

            if (clientNow.canSendMessage(notification)) kordScope.launch {
                clientNow.kordBot.pushMessage(notification, clientNow.locale)
            }
        }
    }

    private fun setupRelayMode(commonConfig: CommonConfig, primaryDataFolder: Path) {
        val relayResult = RelayMode.instance(
            commonConfig = commonConfig,
            dataFolder = primaryDataFolder,
            platformFuncs = logger::severe
        )

        if (relayResult.isSuccess)
            relay = relayResult.getOrThrow()
        else {
            logger.severe("An unrecoverable error occurred!")
            relayResult.exceptionOrNull()?.printStackTrace()
        }

        // Subscribe to event
        relayModeUnregisterListener = TicketManager.EventBus.subscribe<TicketEvent.WithAction> { event ->
            if (event is TicketEvent.CanBeSilent && event.wasSilent) return@subscribe

            val relayNow = relay ?: return@subscribe
            val notification = convertEventToNotification(event, relayNow.locale)

            if (relayNow.canSendMessage(notification))
                server.sendPluginMessage(this, Server2Proxy.DiscordMessage.waterfallString(), notification.serialize())
        }
    }

    private suspend fun setup() {
        fun printUnrecoverableError(e: Throwable) {
            logger.severe("An unrecoverable error occurred!")
            e.printStackTrace()
        }

        val primaryDataFolder = dataFolder.toPath()
            .resolveSibling("TicketManager")
            .resolve("addons")
            .resolve("DiscordBot")

        val commonConfig = CommonConfig.load(primaryDataFolder)
            .unwrapOrReturn {
                printUnrecoverableError(it)
                return
            }

        when (commonConfig.mode) {
            Mode.Enum.CLIENT -> setupClientMode(commonConfig, primaryDataFolder)
            Mode.Enum.RELAY -> setupRelayMode(commonConfig, primaryDataFolder)
        }

        runOnMainThread {
            server.pluginManager.registerEvents(this, this)
            server.messenger.registerOutgoingPluginChannel(this, Server2Proxy.DiscordMessage.waterfallString())
        }
    }

    override fun onEnable() {
        runBlocking { setup() }
        getCommand("tmdiscord")?.setExecutor(this)
    }

    override fun onDisable() {
        clientModeUnregisterListener?.invoke()
        relayModeUnregisterListener?.invoke()

        server.messenger.unregisterOutgoingPluginChannel(this)
        HandlerList.unregisterAll(this as Listener)
        runBlocking {
            client?.kordBot?.logout()
            delay(1000)
            /*
                Above is a shitty workaround for KordLib. shutdown() doesn't fully wait for things to shut down, so
                things are still operating even after the JAR file has been unloaded. Delaying here delays unload to
                hopefully give KordLib time to do what it needs.
             */
        }
        kordThread.close()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.getOrNull(0)?.equals("reload") != true) return false
        if (sender is Player && !sender.hasPermission("tmdiscord.reload")) return false

        runOnMainThread {
            clientModeUnregisterListener?.invoke()
            relayModeUnregisterListener?.invoke()

            // Unregister things
            server.messenger.unregisterOutgoingPluginChannel(this)
            HandlerList.unregisterAll(this as Listener)

            // Restart plugin
            kordScope.launch {
                client?.kordBot?.logout()
                setup()
            }
        }
        return true
    }
}

private fun ProxyChannel.waterfallString() = "$namespace:$channel"