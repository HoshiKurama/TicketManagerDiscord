package com.github.hoshikurama.tmdiscord.mode

import com.github.hoshikurama.tmdiscord.*
import com.github.hoshikurama.tmdiscord.notifications.Notification
import com.github.hoshikurama.tmdiscord.notifications.type
import com.google.common.io.ByteStreams
import java.nio.file.Path

abstract class AbstractClientMode(
    dataFolder: Path,
    classLoader: ClassLoader,
    private val kordBot: KordBot,
) : AbstractPlugin<ClientConfig>(CommonConfigLoader(dataFolder), classLoader) {
    private val locale: ClientLocale

    init {
        val internalLocale = ClientLocale.buildLocaleFromInternal(commonConfig.config.desiredLocaleStr)
        locale = if (config.enableAVC)
            ClientLocale.buildLocaleFromExternal(commonConfig.config.desiredLocaleStr, dataFolder, internalLocale)
        else internalLocale
    }

    override fun buildConfig(
        playerConfig: Map<String, String>,
        internalConfig: Map<String, String>
    ): ClientConfig {
        fun getBooleanValue(ymlStr: String) = playerConfig[ymlStr]?.toBooleanStrictOrNull()
            ?: internalConfig[ymlStr]!!.toBooleanStrict()

        return ClientConfig(
            notifyOnAssign = getBooleanValue("Discord_Notify_On_Assign"),
            notifyOnClose = getBooleanValue("Discord_Notify_On_Close"),
            notifyOnCloseAll = getBooleanValue("Discord_Notify_On_Close_All"),
            notifyOnComment = getBooleanValue("Discord_Notify_On_Comment"),
            notifyOnCreate = getBooleanValue("Discord_Notify_On_Create"),
            notifyOnReopen = getBooleanValue("Discord_Notify_On_Reopen"),
            notifyOnPriorityChange = getBooleanValue("Discord_Notify_On_Priority_Change"),
            enableAVC = getBooleanValue("Enable_avc")
        )
    }

    // Directly Sent
    suspend fun processMessageDirect(notification: Notification) {
        if (allowedToSendMessage(notification.type))
            kordBot.pushMessage(notification, locale)
    }

    // Messaged Relayed
    suspend fun processMessageRelayed(proxyChannel: DiscordChannel, byteArray: ByteArray) {
        when(proxyChannel) {
            is Server2Proxy.DiscordMessage -> {
                val input = ByteStreams.newDataInput(byteArray)
                val type = input.readUTF().run(Notification.Type::valueOf)
                if (!allowedToSendMessage(type)) return

                val msg = Notification.deserialize(type, input, locale)
                kordBot.pushMessage(msg, locale)
            }
            // Future things go here
        }
    }

    suspend fun loginBot() = kordBot.login()

    private fun allowedToSendMessage(type: Notification.Type) = when (type) {
        Notification.Type.ASSIGN -> config.notifyOnAssign
        Notification.Type.CLOSE -> config.notifyOnClose
        Notification.Type.CLOSE_ALL -> config.notifyOnCloseAll
        Notification.Type.COMMENT -> config.notifyOnComment
        Notification.Type.CREATE -> config.notifyOnCreate
        Notification.Type.REOPEN -> config.notifyOnReopen
        Notification.Type.CHANGE_PRIORITY -> config.notifyOnPriorityChange
    }
}

class ClientConfig(
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