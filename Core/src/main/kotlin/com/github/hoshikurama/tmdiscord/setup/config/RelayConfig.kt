package com.github.hoshikurama.tmdiscord.setup.config

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