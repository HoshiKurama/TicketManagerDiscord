package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.mode.CommonConfig
import com.github.hoshikurama.tmdiscord.mode.client.ClientMode
import com.github.hoshikurama.tmdiscord.mode.client.instance
import com.github.hoshikurama.tmdiscord.notifications.Notifications
import kotlinx.coroutines.*
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler

@Suppress("UNUSED")
class WaterfallPlatform: Plugin() {

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val kordThread = newSingleThreadContext("TMDiscordBot_Kord")
    private val kordScope = CoroutineScope(kordThread)
    private lateinit var client: ClientMode

    override fun onEnable() {
        val stdErrorReturn: (Throwable) -> Unit = {
            logger.severe(it.message)
            logger.severe("Plugin failed to enable! Plugin effectively disabled until next restart.")
        }

        val commonConfig = CommonConfig.load(dataFolder.toPath())
            .onFailure {
                stdErrorReturn(it)
                return@onEnable
            }
            .getOrThrow()

        when (commonConfig.mode) {
            PluginMode.RELAY -> {
                logger.severe("Relay Mode is NOT allowed on proxies! Plugin effectively disabled until next restart.")
                return
            }

            PluginMode.CLIENT -> {
                client = runBlocking { ClientMode.instance(commonConfig, dataFolder.toPath()) }
                    .onFailure {
                        stdErrorReturn(it)
                        return@onEnable
                    }
                    .getOrThrow()

                kordScope.launch { client.kordBot.login() } // Launch and hold coroutine on new thread
                Server2Proxy.DiscordMessage
                    .run(ProxyChannel::waterfallString)
                    .run(proxy::registerChannel)
            }
        }
    }

    override fun onDisable() {
        Server2Proxy.DiscordMessage
            .run(ProxyChannel::waterfallString)
            .run(proxy::unregisterChannel)
        runBlocking {
            client.kordBot.logout()
        }
        kordThread.close()
    }

    @EventHandler
    fun onMessage(event: PluginMessageEvent) {
        when (event.tag) {

            Server2Proxy.DiscordMessage.waterfallString() ->
                Notifications.deserialize(event.data, client.locale)
                    .map { notification ->
                        if (client.canSendMessage(notification))
                            kordScope.launch { client.kordBot.pushMessage(notification, client.locale) }
                    }
        }
    }
}

private fun ProxyChannel.waterfallString() = "$namespace:$channel"