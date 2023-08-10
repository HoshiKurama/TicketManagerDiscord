package com.github.hoshikurama.tmdiscord.utility

import java.nio.file.Path

object ConfigWriter {

    inline fun updateConfig(
        loadRawInternalConfig: () -> Result<List<String>>,
        loadRawPlayerConfig: () -> Result<List<String>>,
        outputFilePath: Path,
    ): Result<Unit> = try {
        val isComment: (String) -> Boolean = { it.startsWith("#") }
        val getKey: (String) -> String = { it.split(":")[0] }

        val externalConfig = loadRawPlayerConfig()
            .getOrThrow()
        val externalIdentifiers = externalConfig
            .filterNot(isComment)
            .map(getKey)

        val newValues = loadRawInternalConfig()
            .getOrThrow()
            .map { str ->
                if (!isComment(str) && getKey(str) in externalIdentifiers)
                    externalConfig.first { it.startsWith(getKey(str))}
                else str
            }

        // Write Config file
        val writer = outputFilePath.toFile().bufferedWriter()

        newValues.forEachIndexed { index, str ->
            writer.write(str)

            if (index != newValues.lastIndex)
                writer.newLine()
        }
        writer.close()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}