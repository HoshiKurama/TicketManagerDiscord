package com.github.hoshikurama.tmdiscord.mode.client

import com.github.hoshikurama.tmdiscord.*
import com.github.hoshikurama.tmdiscord.mode.CommonConfig
import com.github.hoshikurama.tmdiscord.mode.standardModeLoad
import com.github.hoshikurama.tmdiscord.notifications.*
import com.github.hoshikurama.tmdiscord.utility.*
import java.nio.file.Path

private const val FILENAME = "config-client.yml"

class ClientConfig(
    // Discord Notifications
    val notifyOnAssign: Boolean,
    val notifyOnClose: Boolean,
    val notifyOnCloseAll: Boolean,
    val notifyOnComment: Boolean,
    val notifyOnCreate: Boolean,
    val notifyOnReopen: Boolean,
    val notifyOnPriorityChange: Boolean,

    // Bot information
    val botToken: String,
    val botChannelSnowflakeID: Long,

    // Misc
    val enableAVC: Boolean,
)


class ClientMode(
    val commonConfig: CommonConfig,
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


suspend fun ClientMode.Companion.instance(commonConfig: CommonConfig, dataFolder: Path): Result<ClientMode> {
    val (clientConfig, clientLocale) = standardModeLoad(
        commonConfig = commonConfig,
        dataFolder = dataFolder,
        filename = FILENAME,
        enableAVC = ClientConfig::enableAVC,
        internalLocaleFolderName = "clientLocales",
        buildInternalLocale = ClientLocale::buildInternalLocale,
        buildExternalLocale = ClientLocale::buildExternalLocale,
        buildConfig = { playerConfigMap, internalConfigMap ->
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
        },
    )
        .onFailure { return Result.failure(it) }
        .getOrThrow()

    // Build KordBot
    val kordBot = KordBot.instance(clientConfig.botToken, clientConfig.botChannelSnowflakeID)
        .onFailure { return Result.failure(it)  }
        .getOrThrow()


    return ClientMode(
        commonConfig = commonConfig,
        clientConfig = clientConfig,
        locale = clientLocale,
        kordBot = kordBot,
    ).run(Result.Companion::success)
}