package com.github.hoshikurama.tmdiscord.mode

import com.github.hoshikurama.tmdiscord.PluginMode
import com.github.hoshikurama.tmdiscord.utility.*
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.exists

private const val FILENAME = "config-common.yml"

class CommonConfig(
    val mode: PluginMode,
    val desiredLocaleStr: String,
    val autoUpdateConfigs: Boolean
) {
    companion object {
        fun load(dataFolder: Path): Result<CommonConfig> {

            // Generate Data Folder
            FileHelper.buildDirectories(dataFolder)
            val configFilePath = dataFolder.resolve(FILENAME)

            // Forces this file to always be updated
            ConfigWriter.updateConfig(
                loadRawInternalConfig = {
                    LoadHelper.loadResource(CommonConfig.classLoader())(FILENAME)
                        .map(InputStream::reader)
                        .map(InputStreamReader::readLines)
                },
                loadRawPlayerConfig = {
                    if (configFilePath.exists())
                        FileHelper.readAllLines(configFilePath)
                    else Result.success(listOf())
                },
                outputFilePath = configFilePath
            )

            // Read configs and propagates any failure
            val playerConfigMap = LoadHelper.loadExternal(configFilePath)
                .map(LoadHelper.inputToConfigMap)
                .onFailure { return Result.failure(it)  }
                .getOrThrow()

            val internalConfigMap = LoadHelper.stdLoadYAMLResource(FILENAME)
                .onFailure { return Result.failure(it)  }
                .getOrThrow()

            // Builds Config
            return CommonConfig(
                mode = playerConfigMap["Plugin_Mode"]?.asOrNull<String>()?.run(PluginMode::valueOf)
                    ?: internalConfigMap["Plugin_Mode"]!!.asOrThrow<String>().run(PluginMode::valueOf),
                desiredLocaleStr = playerConfigMap["Locale"]?.asOrNull<String>()?.lowercase()
                    ?: internalConfigMap["Locale"]!!.asOrThrow<String>().lowercase(),
                autoUpdateConfigs = playerConfigMap["Auto_Update_Configs"]!!.asOrNull<Boolean>()
                    ?: internalConfigMap["Auto_Update_Configs"]!!.asOrThrow<Boolean>()
            ).run(Result.Companion::success)
        }
    }
}

fun CommonConfig.Companion.classLoader(): ClassLoader = CommonConfig::class.java.classLoader