package com.github.hoshikurama.tmdiscord.setup.mode

sealed interface Mode {
    enum class Enum {
        RELAY, CLIENT
    }
}