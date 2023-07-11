package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.Locale
import com.github.hoshikurama.tmdiscord.Targets
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import dev.kord.rest.builder.message.EmbedBuilder
import io.ktor.utils.io.core.*

interface Notification {

    val embedBuilder: EmbedBuilder.(Locale) -> Unit
    fun serialize(): ByteArray

    enum class Type {
        ASSIGN, CLOSE, CLOSE_ALL, COMMENT, CREATE, REOPEN, CHANGE_PRIORITY
    }

    companion object {
        fun deserialize(array: ByteArray, locale: Locale): Notification {
            val input = ByteStreams.newDataInput(array)

            fun computeUser() = Targets.deserialize(input.readUTF(), locale)

            return when (input.readUTF().run(Type::valueOf)) {
                Type.ASSIGN -> Assign(
                    user = computeUser(),
                    ticketID = input.readUTF(),
                    assignment = computeUser(),
                )
                Type.CLOSE -> Close(
                    user = computeUser(),
                    ticketID = input.readUTF(),
                    comment = input.readUTF(),
                )
                Type.CLOSE_ALL -> CloseAll(
                    user = computeUser(),
                    lower = input.readUTF(),
                    upper = input.readUTF(),
                )
                Type.COMMENT -> Comment(
                    user = computeUser(),
                    ticketID = input.readUTF(),
                    comment = input.readUTF(),
                )
                Type.CREATE -> Create(
                    user = computeUser(),
                    ticketID = input.readUTF(),
                    comment = input.readUTF(),
                )
                Type.REOPEN -> Reopen(
                    user = computeUser(),
                    ticketID = input.readUTF(),
                )
                Type.CHANGE_PRIORITY -> ChangePriority(
                    user = computeUser(),
                    ticketID = input.readUTF(),
                    priorityByte = input.readInt()
                )
            }
        }
    }
}

inline fun createByteArrayMessage(f: ByteArrayDataOutput.() -> Unit): ByteArray = ByteStreams.newDataOutput()
    .apply(f)
    .run(ByteArrayDataOutput::toByteArray)
