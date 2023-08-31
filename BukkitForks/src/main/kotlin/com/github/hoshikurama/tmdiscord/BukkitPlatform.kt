package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.ticketmanager.api.paper.events.AsyncTicketModifyEvent
import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.mode.RelayMode
import com.github.hoshikurama.tmdiscord.setup.mode.ClientMode
import com.github.hoshikurama.tmdiscord.setup.mode.Mode
import com.github.hoshikurama.tmdiscord.setup.mode.instance
import com.github.hoshikurama.tmdiscord.setup.shared.CommonLocaleWords
import com.github.hoshikurama.tmdiscord.utility.asTarget
import com.github.hoshikurama.tmdiscord.utility.convertActionInfoToNotification
import com.github.hoshikurama.tmdiscord.utility.unwrapOrReturn
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

@Suppress("unused")
class BukkitPlatform : JavaPlugin(), Listener, CommandExecutor {
    private var client: ClientMode? = null
    private var relay: RelayMode? = null

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
    }

    private fun setupRelayMode(commonConfig: CommonConfig, primaryDataFolder: Path) {
        val relayResult = RelayMode.instance(
            commonConfig = commonConfig,
            dataFolder = primaryDataFolder,
            platformFuncs = logger::severe
        )

        if (relayResult.isSuccess)
            relay = relayResult.getOrThrow()
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
        server.messenger.unregisterOutgoingPluginChannel(this)
        HandlerList.unregisterAll(this as Listener)
        runBlocking {
            client?.kordBot?.logout()
        }
        kordThread.close()
    }

    @EventHandler
    @Suppress("unused")
    fun onTMModify(modifyEvent: AsyncTicketModifyEvent) {
        if (modifyEvent.wasSilent) return

        val relayNow = relay
        val clientNow = client

        val buildNotification = { event: AsyncTicketModifyEvent, locale: CommonLocaleWords ->
            val userTarget = event.commandSender.asTarget(locale)
            convertActionInfoToNotification(
                ticketNumber = event.ticketNumber,
                modOrigin = userTarget,
                action = event.modification,
                commonLocale = locale
            )
        }

        if (relayNow != null) {
            val notification = buildNotification(modifyEvent, relayNow.locale)
            if (relayNow.canSendMessage(notification))
                server.sendPluginMessage(this, Server2Proxy.DiscordMessage.waterfallString(), notification.serialize())
        } else if (clientNow != null) {
            val notification = buildNotification(modifyEvent, clientNow.locale)
            if (clientNow.canSendMessage(notification))
                kordScope.launch { clientNow.kordBot.pushMessage(notification, clientNow.locale) }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.getOrNull(0)?.equals("reload") != true) return false
        if (sender is Player && !sender.hasPermission("tmdiscord.reload")) return false

        runOnMainThread {
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