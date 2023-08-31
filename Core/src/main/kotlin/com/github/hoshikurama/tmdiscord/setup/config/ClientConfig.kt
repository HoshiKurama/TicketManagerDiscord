package com.github.hoshikurama.tmdiscord.setup.config

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