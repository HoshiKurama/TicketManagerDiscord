package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.ticketmanager.api.paper.events.AsyncTicketModifyEvent
import com.github.hoshikurama.tmdiscord.mode.CommonConfig
import com.github.hoshikurama.tmdiscord.mode.relay.RelayMode
import com.github.hoshikurama.tmdiscord.mode.client.ClientMode
import com.github.hoshikurama.tmdiscord.mode.client.instance
import com.github.hoshikurama.tmdiscord.mode.relay.instance
import com.github.hoshikurama.tmdiscord.utility.asTarget
import com.github.hoshikurama.tmdiscord.utility.convertActionInfoToNotification
import kotlinx.coroutines.*
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class BukkitPlatform : JavaPlugin(), Listener {
    private var client: ClientMode? = null
    private var relay: RelayMode? = null

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val kordThread = newSingleThreadContext("TMDiscordBot_Kord")
    private val kordScope = CoroutineScope(kordThread)

    override fun onEnable() {
        val stdErrorReturn: (Throwable) -> Unit = {
            logger.severe(it.message)
            logger.severe("Plugin failed to enable! Shutting down...")
            server.pluginManager.disablePlugin(this)
        }

        val commonConfig = CommonConfig.load(dataFolder.toPath())
            .onFailure {
                stdErrorReturn(it)
                return@onEnable
            }
            .getOrThrow()

        when (commonConfig.mode) {
            PluginMode.RELAY -> {
                relay = RelayMode.instance(commonConfig, dataFolder.toPath())
                    .onFailure {
                        stdErrorReturn(it)
                        return@onEnable
                    }
                    .getOrThrow()
                server.messenger.registerOutgoingPluginChannel(this, Server2Proxy.DiscordMessage.waterfallString())
                server.pluginManager.registerEvents(this, this)
            }

            PluginMode.CLIENT -> {
                client = runBlocking { ClientMode.instance(commonConfig, dataFolder.toPath()) }
                    .onFailure {
                        stdErrorReturn(it)
                        return@onEnable
                    }
                    .getOrThrow()

                kordScope.launch { client?.kordBot?.login() } // Launch and hold coroutine on new thread
                server.pluginManager.registerEvents(this, this)
            }
        }
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
                server.sendPluginMessage(this, Server2Proxy.DiscordMessage.channel, notification.serialize())
        } else if (clientNow != null) {
            val notification = buildNotification(modifyEvent, clientNow.locale)
            if (clientNow.canSendMessage(notification))
                kordScope.launch { clientNow.kordBot.pushMessage(notification, clientNow.locale) }
        }
    }
}

private fun ProxyChannel.waterfallString() = "$namespace:$channel"