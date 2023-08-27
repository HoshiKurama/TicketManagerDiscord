package com.github.hoshikurama.tmdiscord.setup.locale

import com.github.hoshikurama.tmdiscord.setup.shared.CommonLocaleWords
import com.github.hoshikurama.tmdiscord.utility.LoadHelper
import com.github.hoshikurama.tmdiscord.utility.createResult
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

class RelayLocale(
    override val consoleName: String,
    override val nobodyName: String,
) : CommonLocaleWords {
    companion object
}

fun RelayLocale.Companion.buildInternalLocale(localeID: String): Result<RelayLocale> {
    return LoadHelper.stdLoadYAMLResource("relayLocales/$localeID.yml")
        .map { map -> map.mapValues { it.value as String } }
        .map { map ->
            RelayLocale(
                consoleName = map["Console_Name"]!!,
                nobodyName = map["Nobody_Name"]!!,
            )
        }
}

fun RelayLocale.Companion.buildExternalLocale(
    localeID: String,
    dataFolder: Path,
    internalVersion: RelayLocale
): Result<RelayLocale> = createResult {
    val map: Map<String, String> = dataFolder.resolve("relayLocales")
        .resolve("$localeID.yml")
        .run(Files::newInputStream)
        .let { Yaml().load(it) }

    RelayLocale(
        consoleName = map["Console_Name"] ?: internalVersion.consoleName,
        nobodyName = map["Nobody_Name"] ?: internalVersion.nobodyName,
    )
}