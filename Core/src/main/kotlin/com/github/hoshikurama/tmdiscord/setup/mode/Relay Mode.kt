package com.github.hoshikurama.tmdiscord.setup.mode

import com.github.hoshikurama.tmdiscord.notifications.*
import com.github.hoshikurama.tmdiscord.setup.*
import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.config.RelayConfig
import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.setup.locale.RelayLocale
import com.github.hoshikurama.tmdiscord.setup.locale.buildExternalLocale
import com.github.hoshikurama.tmdiscord.setup.locale.buildInternalLocale
import com.github.hoshikurama.tmdiscord.setup.shared.SharedPlatform
import com.github.hoshikurama.tmdiscord.setup.shared.initializeConfig
import com.github.hoshikurama.tmdiscord.setup.shared.initializeLocale
import com.github.hoshikurama.tmdiscord.utility.asOrNull
import com.github.hoshikurama.tmdiscord.utility.asOrThrow
import com.github.hoshikurama.tmdiscord.utility.unwrapOrReturn
import java.nio.file.Path

private const val FILENAME = "config-relay.yml"

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

fun RelayMode.Companion.instance(
    commonConfig: CommonConfig,
    dataFolder: Path,
    platformFuncs: SharedPlatform
): Result<RelayMode> {

    val config = initializeConfig(
        commonConfig = commonConfig,
        dataFolder = dataFolder,
        filename = FILENAME,
        configBuilder = { (playerConfigMap, internalConfigMap) ->
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
    ).unwrapOrReturn { return Result.failure(it) }

    // Build Locale
    val locale = initializeLocale(
        buildInternalLocale = { (name) -> RelayLocale.buildInternalLocale(name) },
        buildExternalLocale = { (r1, r2, r3) -> RelayLocale.buildExternalLocale(r1, r2, r3) },
        localeFolderName = "relayLocales",
        enableAVC = RelayConfig::enableAVC,
        commonConfig = commonConfig,
        dataFolder = dataFolder,
        config = config,
        platformFuncs = platformFuncs
    ).unwrapOrReturn { return Result.failure(it) }

    return RelayMode(
        commonConfig = commonConfig,
        relayConfig = config,
        locale = locale,
    ).run(Result.Companion::success)
}