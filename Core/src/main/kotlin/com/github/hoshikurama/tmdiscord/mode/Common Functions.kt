package com.github.hoshikurama.tmdiscord.mode

import com.github.hoshikurama.tmdiscord.supportedLocales
import com.github.hoshikurama.tmdiscord.utility.*
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.pathString

inline fun <Config, Locale> standardModeLoad(
    commonConfig: CommonConfig,
    dataFolder: Path,
    filename: String,
    internalLocaleFolderName: String,
    buildConfig: (Map<String, Any>, Map<String, Any>) -> Config,
    enableAVC: (Config) -> Boolean,
    buildInternalLocale: (String) -> Result<Locale>,
    buildExternalLocale: (String, Path, Locale) -> Locale,
): Result<Pair<Config, Locale>> {
    val configFilePath = dataFolder.resolve(filename)

    // Generate client config or auto-update...
    if (configFilePath.notExists() || commonConfig.autoUpdateConfigs) {
        val configPathNotExists = configFilePath.notExists()

        // Based on config DNE or auto-update
        val specificRawPlayerConfig: () -> Result<List<String>> =
            if (configPathNotExists) {{ Result.success(listOf()) }}
            else {{ FileHelper.readAllLines(configFilePath) }}

        ConfigWriter.updateConfig(
            loadRawPlayerConfig = specificRawPlayerConfig,
            loadRawInternalConfig = {
                LoadHelper.loadResource(CommonConfig.classLoader())(filename)
                    .map(InputStream::reader)
                    .map(InputStreamReader::readLines)
            },
            outputFilePath = configFilePath
        )

        // Failure point on client config generation
        if (configPathNotExists)
            return resultFailure("Client config file has been generated. This has data which MUST be included prior to operation.")
    }

    // Build Client Config
    val tConfig = run {
        // Read configs and propagates any failure
        val playerConfigMap = LoadHelper.loadExternal(configFilePath)
            .map(LoadHelper.inputToConfigMap)
            .onFailure { return Result.failure(it)  }
            .getOrThrow()

        val internalConfigMap = LoadHelper.stdLoadYAMLResource(filename)
            .onFailure { return Result.failure(it)  }
            .getOrThrow()

        buildConfig(playerConfigMap, internalConfigMap)
    }

    // Build Locale
    val tLocale = if (enableAVC(tConfig)) {
        val avcFolder = dataFolder.resolve(internalLocaleFolderName)
        FileHelper.buildDirectories(avcFolder)

        val internalExternalLocations = supportedLocales
            .map { "$it.yml" }
            .map { Path(internalLocaleFolderName).resolve(it) to avcFolder.resolve(it) }


        // Copy files from internal to external if DNE
        internalExternalLocations
            .filter { (_, external) -> external.notExists() }
            .forEach { (internal, external) ->
                LoadHelper.loadResource(CommonConfig.classLoader())(internal.pathString)
                    .map { stream -> stream.use { input -> Files.copy(input, external) } }
            }

        // Auto-update configs if requested
        if (commonConfig.autoUpdateConfigs) {
            internalExternalLocations.forEach { (internal, external) ->
                ConfigWriter.updateConfig(
                    loadRawInternalConfig = {
                        LoadHelper.loadResource(CommonConfig.classLoader())(internal.pathString)
                            .map(InputStream::reader)
                            .map(InputStreamReader::readLines)
                    },
                    loadRawPlayerConfig = { FileHelper.readAllLines(external) },
                    outputFilePath = external,
                )
            }
        }

        // Build final locale object
        buildInternalLocale(commonConfig.desiredLocaleStr)
            .recoverPossibly { buildInternalLocale("en_ca") }
            .map { // Maps to external here
                buildExternalLocale(
                    commonConfig.desiredLocaleStr,
                    avcFolder,
                    it
                )
            }
    } else {
    buildInternalLocale(commonConfig.desiredLocaleStr)
        .recoverPossibly { buildInternalLocale("en_ca") }
    }
        .onFailure { return Result.failure(it) }
        .getOrThrow()

    return (tConfig to tLocale).run(Result.Companion::success)
}