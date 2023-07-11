package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.notifications.Notification
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import java.lang.Exception

class KordBot private constructor(
    private val kord: Kord,
    private val channel: MessageChannelBehavior,
) {
    suspend fun login() = kord.login()

    companion object {
        suspend fun build(token: String, channelSnowflakeID: Long): KordBot {
            val kord = Kord(token)
            val notifyChannel: MessageChannelBehavior = kord.getChannelOf(id = Snowflake(channelSnowflakeID))
                ?: throw Exception("Invalid Channel Snowflake ID")
            return KordBot(kord, notifyChannel)
        }
    }

    suspend fun pushMessage(msg: Notification, locale: Locale) {
        channel.createEmbed { msg.embedBuilder(this, locale) }
    }
}