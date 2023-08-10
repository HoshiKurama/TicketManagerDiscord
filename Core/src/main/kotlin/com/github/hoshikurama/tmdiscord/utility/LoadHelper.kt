package com.github.hoshikurama.tmdiscord.utility

import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

object LoadHelper {

    val loadResource: (ClassLoader) -> (String) -> Result<InputStream> = { classLoader ->
        { resourcePath ->
            try {
                resourcePath.replace("\\","/")// Evidently you can't use \ for resource paths on Windows
                    .run(classLoader::getResourceAsStream)!!
                    .run(Result.Companion::success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    val loadExternal: (Path) -> Result<InputStream> = { path ->
        try {
            path
                .run(Files::newInputStream)
                .run(Result.Companion::success)
        } catch (e: Exception) { Result.failure(e) }
    }

    val loadYAMLResource: (ClassLoader) -> (String) -> Result<Map<String, Any>> = { classLoader ->
        { pathFromResource -> loadResource(classLoader)(pathFromResource).map(inputToConfigMap) }
    }

    val stdLoadYAMLResource = loadYAMLResource(this::class.java.classLoader)
    val inputToConfigMap: (InputStream) -> Map<String, Any> = { Yaml().load(it) }
}

object FileHelper {

    val readAllLines: (Path) -> Result<List<String>> = { path ->
        try {
            Files.readAllLines(path, Charsets.UTF_8)
                .run(Result.Companion::success)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun buildDirectories(path: Path) = path.toFile().mkdirs()
}