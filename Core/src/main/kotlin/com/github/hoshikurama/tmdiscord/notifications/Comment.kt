package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Comment(
    private val user: Target,
    private val ticketID: String,
    private val comment: String,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name = it.embedOnComment
                .replace("%user%", user.name)
                .replace("%num%", ticketID)
            value = comment
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.COMMENT.toString())
        writeUTF(user.toString())
        writeUTF(ticketID)
        writeUTF(comment)
    }
}