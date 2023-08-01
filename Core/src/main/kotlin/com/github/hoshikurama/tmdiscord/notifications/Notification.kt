package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.ClientLocale
import com.github.hoshikurama.tmdiscord.Targets
import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import dev.kord.rest.builder.message.EmbedBuilder

sealed interface Notification {

    val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit
    fun serialize(): ByteArray

    enum class Type {
        ASSIGN, CLOSE, CLOSE_ALL, COMMENT, CREATE, REOPEN, CHANGE_PRIORITY
    }

    companion object {

        fun deserialize(type: Type, input: ByteArrayDataInput, locale: ClientLocale): Notification {
            fun computeUser() = Targets.deserialize(input.readUTF(), locale)

            return when(type) {
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

        fun deserialize(array: ByteArray, locale: ClientLocale): Notification {
            val input = ByteStreams.newDataInput(array)
            val type = input.readUTF().run(Type::valueOf)

            return deserialize(type, input, locale)
        }
    }
}

val Notification.type: Notification.Type
    get() = when (this) {
        is Assign -> Notification.Type.ASSIGN
        is ChangePriority -> Notification.Type.CHANGE_PRIORITY
        is Close -> Notification.Type.CLOSE
        is CloseAll -> Notification.Type.CLOSE_ALL
        is Comment -> Notification.Type.COMMENT
        is Create -> Notification.Type.CREATE
        is Reopen -> Notification.Type.REOPEN
    }

inline fun createByteArrayMessage(f: ByteArrayDataOutput.() -> Unit): ByteArray = ByteStreams.newDataOutput()
    .apply(f)
    .run(ByteArrayDataOutput::toByteArray)