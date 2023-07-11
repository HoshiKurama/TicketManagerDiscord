package com.github.hoshikurama.tmdiscord

class Locale(
    val consoleName: String,
    val nobodyName: String,

    // Discord Messages
    val embedOnAssign: String,
    val embedOnPriorityChange: String,
    val embedOnClose: String,
    val embedOnCloseAll: String,
    val embedOnComment: String,
    val embedOnCreate: String,
    val embedOnReopen: String,

    // Priority words:
    val priorityLowest: String,
    val priorityLow: String,
    val priorityNormal: String,
    val priorityHigh: String,
    val priorityHighest: String,
)