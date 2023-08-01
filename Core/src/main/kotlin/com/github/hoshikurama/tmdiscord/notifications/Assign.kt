package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Assign(
    private val user: Target,
    private val ticketID: String,
    private val assignment: Target,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            value = assignment.name
            name = it.embedOnAssign
                .replace("%user%", user.name)
                .replace("%num%", ticketID)
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.ASSIGN.toString())
        writeUTF(user.toString())
        writeUTF(ticketID)
        writeUTF(assignment.toString())
    }
}