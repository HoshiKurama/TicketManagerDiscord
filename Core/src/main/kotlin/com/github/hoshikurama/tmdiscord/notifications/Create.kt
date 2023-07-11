package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.Locale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Create(
    private val user: Target,
    private val ticketID: String,
    private val comment: String,
) : Notification {

    override val embedBuilder: EmbedBuilder.(Locale) -> Unit = {
        field {
            name = it.embedOnCreate
                .replace("%user%", user.name)
                .replace("%num%", ticketID)
            value = comment
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CREATE.toString())
        writeUTF(user.toString())
        writeUTF(ticketID)
        writeUTF(comment)
    }
}