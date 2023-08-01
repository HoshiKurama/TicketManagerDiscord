package com.github.hoshikurama.tmdiscord.mode

import com.github.hoshikurama.tmdiscord.CommonConfigLoader

abstract class AbstractRelayMode(
    commonConfigLoader: CommonConfigLoader,
    classLoader4Config: ClassLoader
) : AbstractPlugin<RelayConfig>(commonConfigLoader, classLoader4Config) {

    override fun buildConfig(
        playerConfig: Map<String, String>,
        internalConfig: Map<String, String>
    ): RelayConfig {
        return RelayConfig()
    }

}

class RelayConfig