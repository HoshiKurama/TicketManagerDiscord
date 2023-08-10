package com.github.hoshikurama.tmdiscord

private const val STD_NAMESPACE = "ticketmanagerdiscord"

sealed interface ProxyChannel {
    val namespace: String
    val channel: String
}

object Server2Proxy {
    data object DiscordMessage : ProxyChannel {
        override val namespace = STD_NAMESPACE
        override val channel = "s2p_discord_message"
    }
}