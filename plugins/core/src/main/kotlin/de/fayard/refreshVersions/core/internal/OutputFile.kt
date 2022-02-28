package de.fayard.refreshVersions.core.internal

import de.fayard.refreshVersions.core.internal.VersionCatalogs.LIBS_VERSIONS_TOML
import java.io.File

@InternalRefreshVersionsApi
enum class OutputFile(var path: String, var existed: Boolean = false, val alternativePath: String? = null) {
    OUTPUT_DIR("buildSrc/src/main/kotlin"),
    BUILD("buildSrc/build.gradle.kts", alternativePath = "buildSrc/build.gradle"),
    GIT_IGNORE("buildSrc/.gitignore"),
    LIBS("buildSrc/src/main/kotlin/Libs.kt"),
    VERSIONS_KT("buildSrc/src/main/kotlin/Versions.kt"),
    VERSIONS_PROPERTIES("versions.properties"),
    SETTINGS_GRADLE("settings.gradle"),
    SETTINGS_GRADLE_KTS("settings.gradle.kts"),
    BUILD_SETTINGS_GRADLE("build/settings.gradle"),
    BUILD_SETTINGS_GRADLE_KTS("build/settings.gradle.kts"),
    GRADLE_VERSIONS_CATALOG(LIBS_VERSIONS_TOML),
    ;

    val file get() = rootDir.resolve(path)

    fun readText() =  when {
            file.canRead() -> file.readText()
            alternativePath != null && rootDir.resolve(alternativePath).canRead() -> rootDir.resolve(alternativePath).readText()
            else -> {
                println("${ANSI_RED}Cannot read file $path ${alternativePath ?: ""} $ANSI_RESET")
                error("File not found $this")
            }
        }

    fun writeText(text: String, mustExists: Boolean = false) = when {
        !mustExists -> file.writeText(text)
        file.exists() -> file.writeText(text)
        alternativePath != null && rootDir.resolve(alternativePath).canRead() ->
            rootDir.resolve(alternativePath).writeText(text)
        else -> {
            println("${ANSI_RED}Cannot write file $path ${alternativePath ?: ""} $ANSI_RESET")
            error("File not found $this")
        }
    }


    fun logFileWasModified(delete: Boolean = false) {
        logFileWasModified(path, existed, delete)
    }

    companion object {
        val rootDir = File(".").canonicalFile

        val settingsFiles = listOf(SETTINGS_GRADLE, SETTINGS_GRADLE_KTS, BUILD_SETTINGS_GRADLE, BUILD_SETTINGS_GRADLE_KTS)

        // COLORS
        private const val ANSI_RESET = "\u001B[0m"
        private const val ANSI_GREEN = "\u001B[32m"
        private const val ANSI_RED = "\u001B[31m"
        private const val ANSI_BLUE = "\u001B[34m"

        fun logFileWasModified(path: String, existed: Boolean, delete: Boolean = false) {
            val color = when {
                existed -> ANSI_BLUE
                delete -> ANSI_RED
                else -> ANSI_GREEN
            }
            val status = when {
                delete -> "        deleted:    "
                existed -> "        modified:   "
                else -> "        new file:   "
            }
            println("$color$status$path$ANSI_RESET")
        }

        fun checkWhichFilesExist(rootDir: File) {
            values().forEach { outputFile ->
                outputFile.existed = when {
                    rootDir.resolve(outputFile.path).exists() -> true
                    outputFile.alternativePath == null -> false
                    else -> rootDir.resolve(outputFile.alternativePath).exists()
                }
            }
        }

    }
}
