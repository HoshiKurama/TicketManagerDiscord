package com.github.hoshikurama.tmdiscord

import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

private const val FILENAME = "config-common.yml"

class CommonConfigLoader(val dataFolder: Path) {
    val config: CommonConfig

    init {
        // Generate Data Folder
        if (dataFolder.notExists())
            dataFolder.toFile().mkdir()

        val configFilePath = dataFolder.resolve(FILENAME)

        // Generate Config File
        if (configFilePath.exists())
            updateConfig(
                loadInternalConfig = ::loadCommonConfig,
                loadPlayerConfig = { Files.readAllLines(configFilePath, Charsets.UTF_8) },
                writeFileName = FILENAME,
                writeDataFolder = dataFolder
            )

        // Read Configs
        val playerConfigMap: Map<String, String> = Files.newInputStream(configFilePath)
            .let { Yaml().load(it) }
        val internalConfigMap: Map<String, String> = this::class.java.classLoader
            .getResourceAsStream(FILENAME)
            .let { Yaml().load(it) }

        // Build Config
        config = CommonConfig(
            mode = playerConfigMap["Plugin_Mode"]?.run(PluginMode::valueOf)
                ?: internalConfigMap["Plugin_Mode"]!!.run(PluginMode::valueOf),
            desiredLocaleStr = playerConfigMap["Locale"]?.lowercase()
                ?: internalConfigMap["Locale"]!!.lowercase(),
            autoUpdateConfigs = playerConfigMap["Auto_Update_Configs"]?.toBooleanStrictOrNull()
                ?: internalConfigMap["Auto_Update_Configs"]!!.toBooleanStrict()
        )
    }

    private fun loadCommonConfig() =
        this::class.java.classLoader
            .getResourceAsStream(FILENAME)
            ?.let(InputStream::reader)
            ?.let(InputStreamReader::readLines) ?: emptyList()
}

class CommonConfig(
    val mode: PluginMode,
    val desiredLocaleStr: String,
    val autoUpdateConfigs: Boolean,
)

/*
TODO
On startup, use common builder to build common. This should then determine the plugin and locale to use
 */