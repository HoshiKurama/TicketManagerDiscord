package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.mode.CommonConfig
import com.github.hoshikurama.tmdiscord.mode.client.ClientMode
import com.github.hoshikurama.tmdiscord.mode.client.instance
import com.github.hoshikurama.tmdiscord.notifications.Notifications
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.nio.file.Path

@Suppress("UNUSED")
@Plugin(
    id = "tmdiscordbot",
    name = "TicketManager_Discord_Bot",
    version = "2.0.0",
    description = "Official Discord Bot for TicketManager",
    authors = ["HoshiKurama"],
)
class VelocityPlatform @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory val dataDirectory: Path,
) {
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val kordThread = newSingleThreadContext("TMDiscordBot_Kord")
    private val kordScope = CoroutineScope(kordThread)
    private val discordMessage = Server2Proxy.DiscordMessage.applySettings()
    private lateinit var client: ClientMode


    @Subscribe
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val stdErrorReturn: (Throwable) -> Unit = {
            logger.error(it.message)
            logger.error("Plugin failed to enable! Plugin effectively disabled until next restart.")
        }

        val commonConfig = CommonConfig.load(dataDirectory)
            .onFailure {
                stdErrorReturn(it)
                return@onProxyInitialization
            }
            .getOrThrow()

        when (commonConfig.mode) {
            PluginMode.RELAY -> {
                logger.error("Relay Mode is NOT allowed on proxies! Plugin effectively disabled until next restart.")
                return
            }

            PluginMode.CLIENT -> {
                client = runBlocking { ClientMode.instance(commonConfig, dataDirectory) }
                    .onFailure {
                        stdErrorReturn(it)
                        return@onProxyInitialization
                    }
                    .getOrThrow()

                kordScope.launch { client.kordBot.login() } // Launch and hold coroutine on new thread
                server.channelRegistrar.register(discordMessage)
            }
        }
    }


    @Subscribe
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        server.channelRegistrar.unregister(discordMessage)
        runBlocking {
            client.kordBot.logout()
        }
        kordThread.close()
    }


    @Subscribe
    @Suppress("UNUSED")
    fun onMessage(event: PluginMessageEvent) {
        when (event.identifier) {

            discordMessage ->
                Notifications.deserialize(event.data, client.locale)
                    .map { notification ->
                        if (client.canSendMessage(notification))
                            kordScope.launch { client.kordBot.pushMessage(notification, client.locale) }
                    }
        }
    }
}


private fun ProxyChannel.applySettings() = MinecraftChannelIdentifier.create(namespace, channel)!!