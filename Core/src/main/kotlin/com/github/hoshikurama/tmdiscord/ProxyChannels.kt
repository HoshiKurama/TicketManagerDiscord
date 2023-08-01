package com.github.hoshikurama.tmdiscord

sealed class DiscordChannel(val channel: String) {

    companion object {
        const val namespace = "ticketmanagerdiscord"
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is DiscordChannel) false
        else channel == other.channel
    }

    override fun hashCode() = 31 * channel.hashCode() + namespace.hashCode()
}

object Server2Proxy {
    object DiscordMessage : DiscordChannel("s2p_discord_message")
}