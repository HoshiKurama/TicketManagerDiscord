package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class CloseAll(
    private val user: Target,
    private val lower: String,
    private val upper: String,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name = it.embedOnCloseAll.replace("%user%", user.name)
            value = "#$lower - #$upper"
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CLOSE_ALL.toString())
        writeUTF(user.toString())
        writeUTF(lower)
        writeUTF(upper)
    }
}