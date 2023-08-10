package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.mode.client.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Assign(
    private val user: Target,
    private val ticketID: Long,
    private val assignment: Target
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            value = assignment.name
            name = it.embedOnAssign
                .replace("%user%", user.name)
                .replace("%num%", ticketID.toString())
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.ASSIGN.name)
        writeUTF(user.serialize())
        writeLong(ticketID)
        writeUTF(assignment.toString())
    }
}