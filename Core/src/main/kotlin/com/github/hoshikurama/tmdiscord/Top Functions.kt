package com.github.hoshikurama.tmdiscord

import java.nio.file.Path

internal fun updateConfig(
    loadInternalConfig: () -> List<String>,
    loadPlayerConfig: () -> List<String>,
    writeFileName: String,
    writeDataFolder: Path,
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
    val writer = writeDataFolder.resolve(writeFileName)
        .toFile().bufferedWriter()

    newValues.forEachIndexed { index, str ->
        writer.write(str)

        if (index != newValues.lastIndex)
            writer.newLine()
    }
    writer.close()
}