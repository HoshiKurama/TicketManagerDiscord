package com.github.hoshikurama.tmdiscord.setup.mode

import com.github.hoshikurama.tmdiscord.*
import com.github.hoshikurama.tmdiscord.notifications.*
import com.github.hoshikurama.tmdiscord.setup.config.ClientConfig
import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.locale.ClientLocale
import com.github.hoshikurama.tmdiscord.setup.locale.buildExternalLocale
import com.github.hoshikurama.tmdiscord.setup.locale.buildInternalLocale
import com.github.hoshikurama.tmdiscord.setup.shared.SharedPlatform
import com.github.hoshikurama.tmdiscord.setup.shared.initializeConfig
import com.github.hoshikurama.tmdiscord.setup.shared.initializeLocale
import com.github.hoshikurama.tmdiscord.utility.*
import java.nio.file.Path

private const val FILENAME = "config-client.yml"

class ClientMode(
    val clientConfig: ClientConfig,
    val locale: ClientLocale,
    val kordBot: KordBot,
) {
    companion object

    fun canSendMessage(notification: Notification) = when (notification) {
        is Assign -> clientConfig.notifyOnAssign
        is ChangePriority -> clientConfig.notifyOnPriorityChange
        is Close -> clientConfig.notifyOnClose
        is CloseAll -> clientConfig.notifyOnCloseAll
        is Comment -> clientConfig.notifyOnComment
        is Create -> clientConfig.notifyOnCreate
        is Reopen -> clientConfig.notifyOnReopen
    }
}


suspend fun ClientMode.Companion.instance(
    commonConfig: CommonConfig,
    dataFolder: Path,
    sharedPlatform: SharedPlatform,
): Result<ClientMode> {

    val config = initializeConfig(
        commonConfig = commonConfig,
        dataFolder = dataFolder,
        filename = FILENAME,
        configBuilder = { (playerConfigMap, internalConfigMap) ->
            fun getOrDefault(ymlStr: String) = playerConfigMap[ymlStr]?.asOrNull<Boolean>()
                ?: internalConfigMap[ymlStr]!!.asOrThrow<Boolean>()
            fun missingRequirement(value: String) = resultFailure<ClientMode>("Field $value is missing from the client config but required!")

            ClientConfig(
                notifyOnAssign = getOrDefault("Discord_Notify_On_Assign"),
                notifyOnClose = getOrDefault("Discord_Notify_On_Close"),
                notifyOnCloseAll = getOrDefault("Discord_Notify_On_Close_All"),
                notifyOnComment = getOrDefault("Discord_Notify_On_Comment"),
                notifyOnCreate = getOrDefault("Discord_Notify_On_Create"),
                notifyOnReopen = getOrDefault("Discord_Notify_On_Reopen"),
                notifyOnPriorityChange = getOrDefault("Discord_Notify_On_Priority_Change"),
                enableAVC = getOrDefault("Enable_Advanced_Visual_Control"),
                botToken = playerConfigMap["Discord_Bot_Token"]?.asOrNull<String>()
                    ?: return missingRequirement("Discord_Bot_Token"),
                botChannelSnowflakeID = playerConfigMap["Discord_Channel_ID"]?.asOrNull<String>()?.toLong()
                    ?: return missingRequirement("Discord_Channel_ID")
            )
        }
    ).unwrapOrReturn { return Result.failure(it) }

    // Build Locale
    val locale = initializeLocale(
        buildInternalLocale = { (name) -> ClientLocale.buildInternalLocale(name) },
        buildExternalLocale = { (r1, r2, r3) -> ClientLocale.buildExternalLocale(r1, r2, r3) },
        localeFolderName = "clientLocales",
        enableAVC = ClientConfig::enableAVC,
        commonConfig = commonConfig,
        dataFolder = dataFolder,
        config = config,
        platformFuncs = sharedPlatform
    ).unwrapOrReturn { return Result.failure(it) }

    // Build KordBot
    val kordBot = KordBot.instance(config.botToken, config.botChannelSnowflakeID)
        .unwrapOrReturn { return Result.failure(it)  }

    return ClientMode(
        clientConfig = config,
        locale = locale,
        kordBot = kordBot
    ).run(Result.Companion::success)
}