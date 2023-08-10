package com.github.hoshikurama.tmdiscord.mode.relay

import com.github.hoshikurama.tmdiscord.mode.CommonConfig
import com.github.hoshikurama.tmdiscord.mode.standardModeLoad
import com.github.hoshikurama.tmdiscord.notifications.*
import com.github.hoshikurama.tmdiscord.utility.asOrNull
import com.github.hoshikurama.tmdiscord.utility.asOrThrow
import java.nio.file.Path

private const val FILENAME = "config-relay.yml"

class RelayConfig(
    // Discord Notifications
    val notifyOnAssign: Boolean,
    val notifyOnClose: Boolean,
    val notifyOnCloseAll: Boolean,
    val notifyOnComment: Boolean,
    val notifyOnCreate: Boolean,
    val notifyOnReopen: Boolean,
    val notifyOnPriorityChange: Boolean,

    // Misc
    val enableAVC: Boolean,
)

class RelayMode(
    val commonConfig: CommonConfig,
    val relayConfig: RelayConfig,
    val locale: RelayLocale
) {
    companion object

    fun canSendMessage(notification: Notification) = when (notification) {
        is Assign -> relayConfig.notifyOnAssign
        is ChangePriority -> relayConfig.notifyOnPriorityChange
        is Close -> relayConfig.notifyOnClose
        is CloseAll -> relayConfig.notifyOnCloseAll
        is Comment -> relayConfig.notifyOnComment
        is Create -> relayConfig.notifyOnCreate
        is Reopen -> relayConfig.notifyOnReopen
    }
}

fun RelayMode.Companion.instance(commonConfig: CommonConfig, dataFolder: Path): Result<RelayMode> {
    val (relayConfig, relayLocale) = standardModeLoad(
        commonConfig = commonConfig,
        dataFolder = dataFolder,
        filename = FILENAME,
        enableAVC = RelayConfig::enableAVC,
        internalLocaleFolderName = "relayLocales",
        buildInternalLocale = RelayLocale::buildInternalLocale,
        buildExternalLocale = RelayLocale::buildExternalLocale,
        buildConfig = { playerConfigMap, internalConfigMap ->
            fun getOrDefault(ymlStr: String) = playerConfigMap[ymlStr]?.asOrNull<Boolean>()
                ?: internalConfigMap[ymlStr]!!.asOrThrow<Boolean>()

            RelayConfig(
                notifyOnAssign = getOrDefault("Discord_Notify_On_Assign"),
                notifyOnClose = getOrDefault("Discord_Notify_On_Close"),
                notifyOnCloseAll = getOrDefault("Discord_Notify_On_Close_All"),
                notifyOnComment = getOrDefault("Discord_Notify_On_Comment"),
                notifyOnCreate = getOrDefault("Discord_Notify_On_Create"),
                notifyOnReopen = getOrDefault("Discord_Notify_On_Reopen"),
                notifyOnPriorityChange = getOrDefault("Discord_Notify_On_Priority_Change"),
                enableAVC = getOrDefault("Enable_Advanced_Visual_Control"),
            )
        }
    )
        .onFailure { return Result.failure(it) }
        .getOrThrow()

    return RelayMode(
        commonConfig = commonConfig,
        relayConfig = relayConfig,
        locale = relayLocale,
    ).run(Result.Companion::success)
}