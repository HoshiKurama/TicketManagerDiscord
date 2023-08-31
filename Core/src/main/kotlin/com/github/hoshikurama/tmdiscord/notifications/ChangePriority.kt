package com.github.hoshikurama.tmdiscord.notifications

import com.github.hoshikurama.ticketmanager.api.common.ticket.Ticket
import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.Target
import dev.kord.rest.builder.message.EmbedBuilder

class ChangePriority(
    private val user: Target,
    private val ticketID: Long,
    private val priority: Ticket.Priority,
) : Notification {

    override val embedBuilder: EmbedBuilder.(ClientLocale) -> Unit = {
        field {
            value = when (priority) {
                Ticket.Priority.LOWEST -> it.priorityLowest
                Ticket.Priority.LOW -> it.priorityLow
                Ticket.Priority.NORMAL -> it.priorityNormal
                Ticket.Priority.HIGH -> it.priorityHigh
                Ticket.Priority.HIGHEST -> it.priorityHighest
            }
            name = it.embedOnPriorityChange
                .replace("%user%", user.name)
                .replace("%num%", ticketID.toString())
            inline = false
        }
    }

    override fun serialize(): ByteArray = createByteArrayMessage {
        writeUTF(Notification.Type.CHANGE_PRIORITY.name)
        writeUTF(user.serialize())
        writeLong(ticketID)
        writeUTF(priority.name)
    }
}