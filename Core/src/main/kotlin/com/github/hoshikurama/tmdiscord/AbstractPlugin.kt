package com.github.hoshikurama.tmdiscord

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.notExists

/**
 * Core startup sequence regardless of platform.
 */
abstract class AbstractPlugin<Config : Any>(
    private val mode: PluginMode,
    protected val dataFolder: Path,
    private val classLoader4Config: ClassLoader,
) {
    protected lateinit var config: Config

    // TODO REDO EVERYTHING IN THIS PAGE. BY DEFAULT, RUN IN CLIENT MODE, BUT IF THE THING MUST BE REGENNED, THEN STOP STARTUP AFTER GENERATION
    init {
        // Generate Data Folder
        if (dataFolder.notExists())
            dataFolder.toFile().mkdir()

        // Generate Config File
        val configPath = when(mode) {
            PluginMode.RELAY -> "config-relay.yml"
            PluginMode.CLIENT -> "config-client.yml"
        }.run(dataFolder::resolve)

        if (configPath.notExists())
            updateConfig({ loadInternalConfig(mode) }, { listOf() })

    }

    private fun loadInternalConfig(mode: PluginMode) = classLoader4Config
        .getResourceAsStream("config.yml")
        ?.let(InputStream::reader)
        ?.let(InputStreamReader::readLines) ?: emptyList()

    private fun updateConfig(
        loadInternalConfig: () -> List<String>,
        loadPlayerConfig: () -> List<String>,
    ) {
        val isComment: (String) -> Boolean = { it.startsWith("#") }
        val getKey: (String) -> String = { it.split(":")[0] }

        val externalConfig = loadPlayerConfig() //NOTE: This will not work with future Sponge support
        val externalIdentifiers = externalConfig
            .filterNot(isComment)
            .map(getKey)

        val newValues = loadInternalConfig().map { str ->
            if (!isComment(str) && getKey(str) in externalIdentifiers)
                externalConfig.first { it.startsWith(getKey(str))}
            else str
        }

        // Write Config file
        val writer = when(mode) {
            PluginMode.RELAY -> "config-relay.yml"
            PluginMode.CLIENT -> "config-client.yml"
        }.run(dataFolder::resolve).toFile().bufferedWriter()

        newValues.forEachIndexed { index, str ->
            writer.write(str)

            if (index != newValues.lastIndex)
                writer.newLine()
        }
        writer.close()
    }
}