package com.github.hoshikurama.tmdiscord.setup.shared

import com.github.hoshikurama.tmdiscord.setup.config.CommonConfig
import com.github.hoshikurama.tmdiscord.setup.config.classLoader
import com.github.hoshikurama.tmdiscord.utility.*
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.pathString

data class InternalLocaleBuilderData(val localeName: String)
data class ExternalLocaleBuilderData<Locale>(val localeName: String, val avcFolder: Path, val internalFallback: Locale)
data class ConfigBuilderData(val playerConfigMap: Map<String, Any>, val internalConfigMap: Map<String, Any>)


inline fun <Config> initializeConfig(
    commonConfig: CommonConfig,
    dataFolder: Path,
    filename: String,
    configBuilder: (ConfigBuilderData) -> Config
): Result<Config> {
    val configFilePath = dataFolder.resolve(filename)

    // Generate client config or auto-update...
    if (configFilePath.notExists() || commonConfig.autoUpdateConfigs) {
        val configPathNotExists = configFilePath.notExists()

        // Based on config DNE or auto-update
        ConfigWriter.updateConfig(
            loadRawPlayerConfig = {
                if (configPathNotExists) Result.success(listOf())
                else FileHelper.readAllLines(configFilePath)
            },
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
    val playerConfigMap = LoadHelper.loadExternal(configFilePath)
        .map(LoadHelper.inputToConfigMap)
        .unwrapOrReturn { return Result.failure(it) }

    val internalConfigMap = LoadHelper.stdLoadYAMLResource(filename)
        .unwrapOrReturn { return Result.failure(it) }

    return createResult { ConfigBuilderData(playerConfigMap, internalConfigMap).run(configBuilder) }
}

inline fun <Locale, Config> initializeLocale(
    buildInternalLocale: (InternalLocaleBuilderData) -> Result<Locale>,
    buildExternalLocale: (ExternalLocaleBuilderData<Locale>) -> Result<Locale>,
    platformFuncs: SharedPlatform,
    localeFolderName: String,
    enableAVC: (Config) -> Boolean,
    commonConfig: CommonConfig,
    dataFolder: Path,
    config: Config,
): Result<Locale> {
    if (!enableAVC(config)) {
        return InternalLocaleBuilderData(commonConfig.desiredLocaleStr)
            .run(buildInternalLocale)
            .recover {
                platformFuncs.pushError("Error occurred while building Locale information. Defaulting to en_ca...")
                if (commonConfig.printCaughtErrors) it.printStackTrace()

                InternalLocaleBuilderData("en_ca")
                    .run(buildInternalLocale)
                    .getOrThrow()
            }
    }

    // Else...
    initializeAVC(
        autoUpdateConfigs = commonConfig.autoUpdateConfigs,
        localeFolderName = localeFolderName,
        dataFolder = dataFolder
    )

    // Build final locale object
    return InternalLocaleBuilderData(commonConfig.desiredLocaleStr)
        .run(buildInternalLocale)
        .compose { // Maps to external here
            ExternalLocaleBuilderData(
                localeName =  commonConfig.desiredLocaleStr,
                avcFolder = dataFolder.resolve(localeFolderName),
                internalFallback = it
            ).run(buildExternalLocale)
        }
        .recover {
            platformFuncs.pushError("Error occurred while building Locale information. Defaulting to en_ca...")
            if (commonConfig.printCaughtErrors) it.printStackTrace()

            InternalLocaleBuilderData("en_ca")
                .run(buildInternalLocale)
                .getOrThrow()
        }
}

fun initializeAVC(
    autoUpdateConfigs: Boolean,
    localeFolderName: String,
    dataFolder: Path,
) {
    val avcFolder = dataFolder.resolve(localeFolderName)
    FileHelper.buildDirectories(avcFolder)

    val internalExternalLocations = supportedLocales
        .map { "$it.yml" }
        .map { Path(localeFolderName).resolve(it) to avcFolder.resolve(it) }

    // Copy files from internal to external if DNE
    internalExternalLocations
        .filter { (_, external) -> external.notExists() }
        .forEach { (internal, external) ->
            LoadHelper.loadResource(CommonConfig.classLoader())(internal.pathString)
                .map { stream -> stream.use { input -> Files.copy(input, external) } }
        }

    // Auto-update configs if requested
    if (autoUpdateConfigs) {
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
}