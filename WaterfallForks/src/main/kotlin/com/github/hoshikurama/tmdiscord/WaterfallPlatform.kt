package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.mode.ClientMode
import com.github.hoshikurama.tmdiscord.setup.mode.instance
import com.github.hoshikurama.tmdiscord.notifications.Notifications
import com.github.hoshikurama.tmdiscord.setup.mode.Mode
import com.github.hoshikurama.tmdiscord.utility.unwrapOrReturn
import kotlinx.coroutines.*
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.nio.file.Path

@Suppress("UNUSED")
class WaterfallPlatform: Plugin() {

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val kordThread = newSingleThreadContext("TMDiscordBot_Kord")
    private val kordScope = CoroutineScope(kordThread)
    private var client: ClientMode? = null


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
            Mode.Enum.RELAY -> {
                logger.severe("Relay Mode is NOT allowed on proxies!")
                return
            }
        }
    }

    override fun onEnable() {
        runBlocking { setup() }

        Server2Proxy.DiscordMessage
            .run(ProxyChannel::waterfallString)
            .run(proxy::registerChannel)

        // Register command
        val reload = object : Command("ticket") {
            override fun execute(sender: CommandSender, args: Array<String>) {
                if (args.getOrNull(0)?.equals("reload") != true) return
                if (!sender.hasPermission("tmdiscord")) return

                kordScope.launch {
                    client?.kordBot?.logout()
                    setup()
                }
            }
        }
        proxy.pluginManager.registerCommand(this, reload)
    }

    override fun onDisable() {
        Server2Proxy.DiscordMessage
            .run(ProxyChannel::waterfallString)
            .run(proxy::unregisterChannel)
        runBlocking { client?.kordBot?.logout() }
        kordThread.close()
    }

    @EventHandler
    fun onMessage(event: PluginMessageEvent) {
        val clientCopy = client

        if (event.tag == Server2Proxy.DiscordMessage.waterfallString() && clientCopy != null) {
            Notifications.deserialize(event.data, clientCopy.locale)
                .unwrapOrReturn { return }
                .takeIf(clientCopy::canSendMessage)
                ?.let { noti -> kordScope.launch { client!!.kordBot.pushMessage(noti, clientCopy.locale) } }
        }
    }
}

private fun ProxyChannel.waterfallString() = "$namespace:$channel"