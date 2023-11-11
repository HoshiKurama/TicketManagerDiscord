package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class CloseAll(
    private val user: Target,
    private val lower: Long,
    private val upper: Long,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            name = it.embedOnCloseAll.replace("%user%", user.name)
            value = "#$lower - #$upper"
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CLOSE_ALL.name)
        writeUTF(user.serialize())
        writeLong(lower)
        writeLong(upper)
    }
}