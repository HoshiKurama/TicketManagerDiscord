package com.github.hoshikurama.tmdiscord.mode

import com.github.hoshikurama.tmdiscord.*
import com.github.hoshikurama.tmdiscord.updateConfig
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import kotlin.io.path.notExists

/**
 * Core startup sequence regardless of platform.
 */
abstract class AbstractPlugin<Config : Any>(
    protected val commonConfig: CommonConfigLoader,
    private val classLoader4Config: ClassLoader,
) {
    protected val config: Config

    private val dataFolder = commonConfig.dataFolder
    private val configFileName = when(commonConfig.config.mode) {
        PluginMode.RELAY -> "config-relay.yml"
        PluginMode.CLIENT -> "config-client.yml"
    }

    init {
        // Generate Data Folder
        if (dataFolder.notExists())
            dataFolder.toFile().mkdir()

        // Generate Config Path
        val configPath = dataFolder.resolve(configFileName)

        // Generate Config file and stop startup if file DNE
        if (configPath.notExists()) {
            updateConfig(
                loadInternalConfig = ::loadInternalConfig,
                loadPlayerConfig = { listOf() },
                writeFileName = configFileName,
                writeDataFolder = dataFolder
            )
            throw ConfigNotFoundException()
        }

        // Read Configs
        val playerConfigMap: Map<String, String> = Files.newInputStream(configPath)
            .let { Yaml().load(it) }
        val internalConfigMap: Map<String, String> = this::class.java.classLoader
            .getResourceAsStream(configFileName)
            .let { Yaml().load(it) }

        @Suppress("LeakingThis") // This is okay as it should never rely on derived class state
        config = buildConfig(playerConfigMap, internalConfigMap)

        // Auto-Update Config
        if (commonConfig.config.autoUpdateConfigs) {
            updateConfig(
                loadInternalConfig = ::loadInternalConfig,
                loadPlayerConfig = { Files.readAllLines(configPath, Charsets.UTF_8) },
                writeFileName = configFileName,
                writeDataFolder = dataFolder
            )
        }
    }

    abstract fun buildConfig(
        playerConfig: Map<String, String>,
        internalConfig: Map<String, String>,
    ): Config

    private fun loadInternalConfig() = classLoader4Config
        .getResourceAsStream(configFileName)
        ?.let(InputStream::reader)
        ?.let(InputStreamReader::readLines) ?: emptyList()
}