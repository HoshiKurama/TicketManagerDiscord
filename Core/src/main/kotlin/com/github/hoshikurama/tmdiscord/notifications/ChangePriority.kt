package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class ChangePriority(
    private val user: Target,
    private val ticketID: String,
    private val priorityByte: Int,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            value = when (priorityByte) {
                1 -> it.priorityLowest
                2 -> it.priorityLow
                3 -> it.priorityNormal
                4 -> it.priorityHigh
                5 -> it.priorityHighest
                else -> throw Exception("Invalid Priority Level while attempting to create Discord embed field")
            }
            name = it.embedOnPriorityChange
                .replace("%user%", user.name)
                .replace("%num%", ticketID)
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CHANGE_PRIORITY.toString())
        writeUTF(user.toString())
        writeUTF(ticketID)
        writeByte(priorityByte)
    }
}