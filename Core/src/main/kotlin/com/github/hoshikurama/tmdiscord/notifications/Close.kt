package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Close(
    private val user: Target,
    private val ticketID: String,
    private val comment: String?,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name =  it.embedOnClose
                .replace("%user%", user.name)
                .replace("%num%", ticketID)
            value = comment ?: " "
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CLOSE.toString())
        writeUTF(user.toString())
        writeUTF(ticketID)
        writeUTF(comment ?: " ")
    }
}