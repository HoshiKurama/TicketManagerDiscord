package com.github.hoshikurama.tmdiscord.utility

import com.github.hoshikurama.ticketmanager.api.common.commands.CommandSender
import com.github.hoshikurama.ticketmanager.api.common.ticket.Action
import com.github.hoshikurama.ticketmanager.api.common.ticket.ActionInfo
import com.github.hoshikurama.ticketmanager.api.common.ticket.Assignment
import com.github.hoshikurama.tmdiscord.setup.shared.CommonLocaleWords
import com.github.hoshikurama.tmdiscord.Target
import com.github.hoshikurama.tmdiscord.Targets
import com.github.hoshikurama.tmdiscord.notifications.*

val supportedLocales = setOf("de_de", "en_ca", "en_uk", "en_us")

// Inline (safe) type casting

inline fun <reified T> Any.asOrNull() = this as? T
inline fun <reified T> Any.asOrThrow() = this as T

// Result Stuff

fun <T> resultFailure(msg: String): Result<T> = Exception(msg)
    .let(Result.Companion::failure)

inline fun <T> Result<T>.recoverPossibly(transform: (Throwable) -> Result<T>): Result<T> {
    return this.recover { throwable ->
        transform(throwable)
            .onFailure { return Result.failure(it) }
            .getOrThrow()
    }
}

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
    TODO("Invalid use of unwrapOrReturn(). Make sure to perform a local return where called!")
}

// Other

fun CommandSender.Active.asTarget(locale: CommonLocaleWords) = when (this) {
    is CommandSender.Active.OnlineConsole -> Targets.Console(locale.consoleName)
    is CommandSender.Active.OnlinePlayer -> Targets.User(username)
}

fun Assignment.toTarget(locale: CommonLocaleWords) = when (this) {
    Assignment.Console -> Targets.Console(locale.consoleName)
    Assignment.Nobody -> Targets.Nobody(locale.nobodyName)
    is Assignment.PermissionGroup -> Targets.Group(permissionGroup)
    is Assignment.Phrase -> Targets.Phrase(phrase)
    is Assignment.Player -> Targets.User(username)
}

fun convertActionInfoToNotification(
    ticketNumber: Long,
    modOrigin: Target,
    action: Action,
    commonLocale: CommonLocaleWords
) = when (action) {
    is ActionInfo.Assign -> Assign(
        assignment = action.assignment.toTarget(commonLocale),
        ticketID = ticketNumber,
        user = modOrigin,
    )
    is ActionInfo.CloseWithComment -> Close(
        comment = action.comment,
        ticketID = ticketNumber,
        user = modOrigin,
    )
    is ActionInfo.CloseWithoutComment -> Close(
        ticketID = ticketNumber,
        user = modOrigin,
        comment = null,
    )
    is ActionInfo.Comment -> Comment(
        comment = action.comment,
        ticketID = ticketNumber,
        user = modOrigin,
    )
    is ActionInfo.MassClose -> CloseAll(
        user = modOrigin,
    )
    is ActionInfo.Open -> Create(
        comment = action.message,
        ticketID = ticketNumber,
        user = modOrigin,
    )
    is ActionInfo.Reopen -> Reopen(
        ticketID = ticketNumber,
        user = modOrigin,
    )
    is ActionInfo.SetPriority -> ChangePriority(
        priority = action.priority,
        ticketID = ticketNumber,
        user = modOrigin,
    )
}