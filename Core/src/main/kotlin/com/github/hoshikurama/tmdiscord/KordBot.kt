package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.notifications.Notification
import com.github.hoshikurama.tmdiscord.utility.resultFailure
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed

class KordBot(
    private val kord: Kord,
    private val channel: MessageChannelBehavior,
) {
    suspend fun login() = kord.login()
    suspend fun logout() = kord.shutdown()

    suspend fun pushMessage(msg: Notification, locale: ClientLocale) {
        channel.createEmbed { msg.embedBuilder(this, locale) }
    }

    companion object {

        suspend fun instance(token: String, channelID: Long): Result<KordBot> {
            return try {
                val kord = Kord(token)
                val notifyChannel: MessageChannelBehavior = kord.getChannelOf(id = Snowflake(channelID))
                    ?: return resultFailure("Invalid Channel ID: $channelID")

                KordBot(kord, notifyChannel)
                    .run(Result.Companion::success)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}