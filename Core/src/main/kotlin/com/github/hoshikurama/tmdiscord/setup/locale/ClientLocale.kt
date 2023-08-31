package com.github.hoshikurama.tmdiscord.setup.locale


import com.github.hoshikurama.tmdiscord.setup.shared.CommonLocaleWords
import com.github.hoshikurama.tmdiscord.utility.*
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path

class ClientLocale(
    // Names
    override val consoleName: String,
    override val nobodyName: String,

    // Discord Messages
    val embedOnAssign: String,
    val embedOnPriorityChange: String,
    val embedOnClose: String,
    val embedOnCloseAll: String,
    val embedOnComment: String,
    val embedOnCreate: String,
    val embedOnReopen: String,

    // Priority words:
    val priorityLowest: String,
    val priorityLow: String,
    val priorityNormal: String,
    val priorityHigh: String,
    val priorityHighest: String,
): CommonLocaleWords {
    companion object
}

fun ClientLocale.Companion.buildInternalLocale(localeID: String): Result<ClientLocale> {
    return LoadHelper.stdLoadYAMLResource("clientLocales/$localeID.yml")
        .map { map -> map.mapValues { it.value as String } }
        .map { map ->
            ClientLocale(
                consoleName = map["Console_Name"]!!,
                nobodyName = map["Nobody_Name"]!!,
                embedOnAssign = map["Embed_OnAssign"]!!,
                embedOnClose = map["Embed_OnClose"]!!,
                embedOnCloseAll = map["Embed_OnCloseAll"]!!,
                embedOnComment = map["Embed_OnComment"]!!,
                embedOnCreate = map["Embed_OnCreate"]!!,
                embedOnReopen = map["Embed_OnReopen"]!!,
                embedOnPriorityChange = map["Embed_OnPriorityChange"]!!,
                priorityLowest = map["Priority_Lowest"]!!,
                priorityLow = map["Priority_Low"]!!,
                priorityNormal = map["Priority_Normal"]!!,
                priorityHigh = map["Priority_High"]!!,
                priorityHighest = map["Priority_Highest"]!!,
            )
        }
}

fun ClientLocale.Companion.buildExternalLocale(
    localeID: String,
    dataFolder: Path,
    internalVersion: ClientLocale
): Result<ClientLocale> = createResult {
    val map: Map<String, String> = dataFolder.resolve("clientLocales")
        .resolve("$localeID.yml")
        .run(Files::newInputStream)
        .let { Yaml().load(it) }

    ClientLocale(
        consoleName = map["Console_Name"] ?: internalVersion.consoleName,
        nobodyName = map["Nobody_Name"] ?: internalVersion.nobodyName,
        embedOnAssign = map["Embed_OnAssign"] ?: internalVersion.embedOnAssign,
        embedOnClose = map["Embed_OnClose"] ?: internalVersion.embedOnClose,
        embedOnCloseAll = map["Embed_OnCloseAll"] ?: internalVersion.embedOnCloseAll,
        embedOnComment = map["Embed_OnComment"] ?: internalVersion.embedOnComment,
        embedOnCreate = map["Embed_OnCreate"] ?: internalVersion.embedOnCreate,
        embedOnReopen = map["Embed_OnReopen"] ?: internalVersion.embedOnReopen,
        embedOnPriorityChange = map["Embed_OnPriorityChange"] ?: internalVersion.embedOnPriorityChange,
        priorityLowest = map["Priority_Lowest"] ?: internalVersion.priorityLowest,
        priorityLow = map["Priority_Low"] ?: internalVersion.priorityLow,
        priorityNormal = map["Priority_Normal"] ?: internalVersion.priorityNormal,
        priorityHigh = map["Priority_High"] ?: internalVersion.priorityHigh,
        priorityHighest = map["Priority_Highest"] ?: internalVersion.priorityHighest,
    )
}
