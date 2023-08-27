package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.mode.ClientMode
import com.github.hoshikurama.tmdiscord.setup.mode.instance
import com.github.hoshikurama.tmdiscord.notifications.Notifications
import com.github.hoshikurama.tmdiscord.setup.mode.Mode
import com.github.hoshikurama.tmdiscord.utility.unwrapOrReturn
import com.google.inject.Inject
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
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
    @DataDirectory private val dataDirectory: Path,
) {
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val kordThread = newSingleThreadContext("TMDiscordBot_Kord")
    private val kordScope = CoroutineScope(kordThread)
    private val discordMessage = Server2Proxy.DiscordMessage.applySettings()
    private var client: ClientMode? = null


    private suspend fun setupClientMode(commonConfig: CommonConfig, primaryDataFolder: Path) {
        val clientResult = ClientMode.instance(
            commonConfig = commonConfig,
            dataFolder = primaryDataFolder,
            sharedPlatform = logger::error
        )

        if (clientResult.isSuccess) {
            client = clientResult.getOrThrow()
            kordScope.launch { client?.kordBot?.login() }
        } else {
            logger.error("An unrecoverable error occurred!")
            clientResult.exceptionOrNull()?.printStackTrace()
        }
    }

    private suspend fun setup() {

        fun printUnrecoverableError(e: Throwable) {
            logger.error("An unrecoverable error occurred!")
            e.printStackTrace()
        }

        val primaryDataFolder = dataDirectory
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
                logger.error("Relay Mode is NOT allowed on proxies!")
                return
            }
        }
    }


    @Subscribe
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        runBlocking { setup() }
        server.channelRegistrar.register(discordMessage)

        LiteralArgumentBuilder
            .literal<CommandSource>("tmdiscord")
            .requires { it.hasPermission("tmdiscord.reload") }
            .then(LiteralArgumentBuilder.literal<CommandSource>("reload")
                .executes {
                    kordScope.launch {
                        client?.kordBot?.logout()
                        setup()
                    }
                    Command.SINGLE_SUCCESS
                }
            )
            .build()
            .run(::BrigadierCommand)
            .run(server.commandManager::register)
    }


    @Subscribe
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        server.channelRegistrar.unregister(discordMessage)
        runBlocking { client?.kordBot?.logout() }
        kordThread.close()
    }


    @Subscribe
    @Suppress("UNUSED")
    fun onMessage(event: PluginMessageEvent) {
        val clientCopy = client

        if (event.identifier == discordMessage && clientCopy != null) {
            Notifications.deserialize(event.data, clientCopy.locale)
                .unwrapOrReturn { return }
                .takeIf(clientCopy::canSendMessage)
                ?.let { noti -> kordScope.launch { client!!.kordBot.pushMessage(noti, clientCopy.locale) } }
        }
    }
}


private fun ProxyChannel.applySettings() = MinecraftChannelIdentifier.create(namespace, channel)!!