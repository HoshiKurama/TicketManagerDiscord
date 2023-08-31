package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Create(
    private val user: Target,
    private val ticketID: Long,
    private val comment: String,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name = it.embedOnCreate
                .replace("%user%", user.name)
                .replace("%num%", ticketID.toString())
            value = comment
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CREATE.name)
        writeUTF(user.serialize())
        writeLong(ticketID)
        writeUTF(comment)
    }
}