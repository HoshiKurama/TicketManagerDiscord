package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.ticketmanager.api.common.ticket.Ticket
import com.github.hoshikurama.tmdiscord.mode.client.ClientLocale
import com.github.hoshikurama.tmdiscord.Targets
import com.github.hoshikurama.tmdiscord.mode.client.ClientConfig
import com.github.hoshikurama.tmdiscord.notifications.Notification.Type.*
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import dev.kord.rest.builder.message.EmbedBuilder

sealed interface Notification {
    val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit
    fun serialize(): ByteArray

    enum class Type {
        ASSIGN, CLOSE, CLOSE_ALL, COMMENT, CREATE, REOPEN, CHANGE_PRIORITY
    }
}

object Notifications {

    fun deserialize(array: ByteArray, locale: ClientLocale): Result<Notification> {
        val input = ByteStreams.newDataInput(array)
        val type = try { input.readUTF().run(Notification.Type::valueOf) }
                   catch (e: Exception) { return Result.failure(e) }

        fun computeUser() = Targets.deserialize(input.readUTF(), locale)

        return when(type) {
            ASSIGN -> Assign(
                user = computeUser(),
                ticketID = input.readLong(),
                assignment = computeUser(),
            )
            CLOSE -> Close(
                user = computeUser(),
                ticketID = input.readLong(),
                comment = input.readUTF(),
            )
            CLOSE_ALL -> CloseAll(
                user = computeUser(),
                //lower = input.readLong(),
                //upper = input.readLong(),
            )
            COMMENT -> Comment(
                user = computeUser(),
                ticketID = input.readLong(),
                comment = input.readUTF(),
            )
            CREATE -> Create(
                user = computeUser(),
                ticketID = input.readLong(),
                comment = input.readUTF(),
            )
            REOPEN -> Reopen(
                user = computeUser(),
                ticketID = input.readLong(),
            )
            CHANGE_PRIORITY -> ChangePriority(
                user = computeUser(),
                ticketID = input.readLong(),
                priority = input.readUTF().run(Ticket.Priority::valueOf)
            )
        }.let(Result.Companion::success)
    }
}

inline fun createByteArrayMessage(f: ByteArrayDataOutput.() -> Unit): ByteArray = ByteStreams.newDataOutput()
    .apply(f).run(ByteArrayDataOutput::toByteArray)