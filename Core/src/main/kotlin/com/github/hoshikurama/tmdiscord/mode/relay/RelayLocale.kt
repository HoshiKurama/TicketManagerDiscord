package com.github.hoshikurama.tmdiscord.mode.relay

import com.github.hoshikurama.tmdiscord.CommonLocaleWords
import com.github.hoshikurama.tmdiscord.utility.LoadHelper
import com.github.hoshikurama.tmdiscord.utility.resultFailure
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

class RelayLocale(
    override val consoleName: String,
    override val nobodyName: String,
) : CommonLocaleWords {
    companion object
}

fun RelayLocale.Companion.buildInternalLocale(localeID: String): Result<RelayLocale> {
    fun loadYML(locale: String) = Path("relayLocales")
        .resolve("$locale.yml")
        .pathString
        .run(LoadHelper.stdLoadYAMLResource)
        .getOrThrow()

    val map =
        try { loadYML(localeID).mapValues { it.value as String } }
        catch (e: Exception) { return resultFailure("Could not read locale: $localeID or an unexpected error has occurred!") }

    return RelayLocale(
        consoleName = map["Console_Name"]!!,
        nobodyName = map["Nobody_Name"]!!,
    ).run(Result.Companion::success)
}

fun RelayLocale.Companion.buildExternalLocale(
    localeID: String,
    dataFolder: Path,
    internalVersion: RelayLocale
): RelayLocale {
    val map: Map<String, String> = try {
        dataFolder.resolve("clientLocales")
            .resolve("$localeID.yml")
            .run(Files::newInputStream)
            .let { Yaml().load(it) }
    } catch (e: Exception) { mapOf() }

    return RelayLocale(
        consoleName = map["Console_Name"] ?: internalVersion.consoleName,
        nobodyName = map["Nobody_Name"] ?: internalVersion.nobodyName,
    )
}