package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class Reopen(
    private val user: Target,
    private val ticketID: Long,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name = it.embedOnReopen
                .replace("%user%", user.name)
                .replace("%num%", ticketID.toString())
            value = "⠀"
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.REOPEN.name)
        writeUTF(user.serialize())
        writeLong(ticketID)
    }
}