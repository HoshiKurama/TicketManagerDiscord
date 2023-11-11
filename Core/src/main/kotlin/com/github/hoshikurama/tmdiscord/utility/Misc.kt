package com.github.hoshikurama.tmdiscord.utility

import com.github.hoshikurama.ticketmanager.api.CommandSender
import com.github.hoshikurama.ticketmanager.api.events.*
import com.github.hoshikurama.ticketmanager.api.ticket.Assignment
import com.github.hoshikurama.tmdiscord.setup.shared.CommonLocaleWords
import com.github.hoshikurama.tmdiscord.Targets
import com.github.hoshikurama.tmdiscord.notifications.*

val supportedLocales = setOf("de_de", "en_ca", "en_uk", "en_us")

// Inline (safe) type casting

inline fun <reified T> Any.asOrNull() = this as? T
inline fun <reified T> Any.asOrThrow() = this as T

// Result Stuff

fun <T> resultFailure(msg: String): Result<T> = Exception(msg)
    .let(Result.Companion::failure)

inline fun <T, U> Result<T>.compose(f: (T) -> Result<U>): Result<U> {
    if (isFailure) return Result.failure(exceptionOrNull()!!)
    return try { getOrThrow().run(f) }
    catch (e: Exception) { Result.failure(e) }
}

inline fun <T> createResult(f: () -> T): Result<T> {
    return try { f().run(Result.Companion::success) }
    catch (e: Exception) { Result.failure(e) }
}

inline fun <T> Result<T>.unwrapOrReturn(onError: (Throwable) -> Unit): T {
    if (isSuccess) return getOrThrow()
    onError(exceptionOrNull()!!)
    throw Exception("Invalid use of unwrapOrReturn(). Make sure to perform a local return where called!")
}

// Other

fun CommandSender.Active.asTarget(locale: CommonLocaleWords) = when (this) {
    is CommandSender.OnlineConsole -> Targets.Console(locale.consoleName)
    is CommandSender.OnlinePlayer -> Targets.User(username)
}

fun Assignment.toTarget(locale: CommonLocaleWords) = when (this) {
    Assignment.Console -> Targets.Console(locale.consoleName)
    Assignment.Nobody -> Targets.Nobody(locale.nobodyName)
    is Assignment.PermissionGroup -> Targets.Group(permissionGroup)
    is Assignment.Phrase -> Targets.Phrase(phrase)
    is Assignment.Player -> Targets.User(username)
}

fun convertEventToNotification(
    event: TicketEvent.WithAction,
    locale: CommonLocaleWords,
) = when (event) {
    is TicketAssignEvent -> Assign(
        assignment = event.action.assignment.toTarget(locale),
        user = event.commandSender.asTarget(locale),
        ticketID = event.id,
    )
    is TicketCloseWithCommentEvent -> Close(
        user = event.commandSender.asTarget(locale),
        comment = event.action.comment,
        ticketID = event.id,
    )
    is TicketCloseWithoutCommentEvent -> Close(
        user = event.commandSender.asTarget(locale),
        ticketID = event.id,
        comment = null,
    )
    is TicketCommentEvent -> Comment(
        user = event.commandSender.asTarget(locale),
        comment = event.action.comment,
        ticketID = event.id,
    )
    is TicketCreateEvent -> Create(
        user = event.commandSender.asTarget(locale),
        comment = event.action.message,
        ticketID = event.id,
    )
    is TicketReopenEvent -> Reopen(
        user = event.commandSender.asTarget(locale),
        ticketID = event.id,
    )
    is TicketSetPriorityEvent -> ChangePriority(
        user = event.commandSender.asTarget(locale),
        priority = event.action.priority,
        ticketID = event.id,
    )
    is TicketMassCloseEvent -> CloseAll(
        user = event.commandSender.asTarget(locale),
        upper = event.upperBound,
        lower = event.lowerBound,
    )
}