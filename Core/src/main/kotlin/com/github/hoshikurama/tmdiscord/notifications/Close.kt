package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.mode.client.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Close(
    private val user: Target,
    private val ticketID: Long,
    private val comment: String?,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name =  it.embedOnClose
                .replace("%user%", user.name)
                .replace("%num%", ticketID.toString())
            value = comment ?: " "
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CLOSE.name)
        writeUTF(user.serialize())
        writeLong(ticketID)
        writeUTF(comment ?: " ")
    }
}