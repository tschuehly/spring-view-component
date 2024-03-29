package de.tschuehly.spring.viewcomponent.core.processor

import de.tschuehly.spring.viewcomponent.core.action.*
import de.tschuehly.spring.viewcomponent.jte.JteViewComponentCompiler
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.annotation.processing.Messager
import javax.tools.Diagnostic
import kotlin.io.path.createDirectories

class ViewComponentParser(
    private val srcFile: File,
    private val buildType: BuildType,
    private val methodList: List<ViewActionMethod>,
    private val viewComponentName: String,
    private val messager: Messager? = null
) {


    enum class BuildType {
        MAVEN, GRADLE
    }

    enum class Language(val directoryName: String) {
        KOTLIN("kotlin"), JAVA("java")
    }

    fun parseFile(isLiveReload: Boolean): String? {
        val parsedHtml = parseSrcHtmlFile()
        val (rootDir, packagePath) = getRootDirAndPackagePath(srcFile, messager)
        val resourceDirPath = getResourceDirPath(rootDir, packagePath)
        val resourceHtmlFile = getResourceFile(resourceDirPath)
        resourceHtmlFile.writeAll(parsedHtml)
        if (resourceHtmlFile.extension == "jte" || resourceHtmlFile.extension == "kte") {
            return compileJte(rootDir, isLiveReload, resourceDirPath, resourceHtmlFile, packagePath)
        }
        return null
    }

    private fun compileJte(
        rootDir: String,
        isLiveReload: Boolean,
        resourceDirPath: Path,
        resourceHtmlFile: File,
        packagePath: String
    ): String? {
        val compiler = JteViewComponentCompiler()

        val packageName = packagePath.replace(FileSystems.getDefault().separator, ".").let {
            it.substring(0, it.length - 1)
        }

        val language = getLanguage(resourceHtmlFile)

        if (!isLiveReload) {
            val classDir = getGeneratedSourcesDir(rootDir,language)
            val file = compiler.generate(
                rootDir = resourceDirPath.toAbsolutePath(),
                names = srcFile.name,
                classDirectory = listOf(
                    classDir.toAbsolutePath().toString()
                ),
                packageName
            )
            resourceHtmlFile.delete()
            return file
        }
        resourceHtmlFile.delete()
        return null
    }


    private fun getCompileDirectory(resourceHtmlFile: File, rootDir: String): String {
        if (buildType == BuildType.GRADLE) {
            val language = getLanguage(resourceHtmlFile)
            return FileSystems.getDefault()
                .getPath(
                    rootDir,
                    "build",
                    "classes",
                    language.directoryName,
                    "main"
                ).toAbsolutePath().toString()
        }

        return FileSystems.getDefault()
            .getPath(
                rootDir,
                "target",
                "classes"
            ).toAbsolutePath().toString()
    }

    private fun getLanguage(resourceHtmlFile: File): Language {
        return if (resourceHtmlFile.extension == "kte") {
            Language.KOTLIN
        } else {
            Language.JAVA
        }
    }

    private fun File.writeAll(
        parsedHtml: List<String>
    ) {
        val writer = this.printWriter()
        parsedHtml.forEach {
            writer.println(it)
        }
        writer.flush()
        writer.close()
    }

    private fun getResourceDirPath(rootDir: String, packagePath: String): Path = if (buildType == BuildType.GRADLE) {
        FileSystems.getDefault()
            .getPath(rootDir, "build", "resources", "main", packagePath)
    } else {
        FileSystems.getDefault()
            .getPath(rootDir, "target", "classes", packagePath)
    }

    private fun getGeneratedSourcesDir(rootDir: String, language: Language): Path {
        return if (buildType == BuildType.GRADLE && language == Language.KOTLIN) {
            FileSystems.getDefault()
                .getPath(rootDir, "build", "generated", "source", "kapt", "main")
        } else if (buildType == BuildType.GRADLE && language == Language.JAVA) {
            FileSystems.getDefault()
                .getPath(rootDir, "build", "generated", "sources", "annotationProcessor", "java", "main")
        } else {
            FileSystems.getDefault()
                .getPath(rootDir, "target", "generated-sources", "annotations")
        }
    }

    private fun getResourceFile(resourceDir: Path): File {
        resourceDir.createDirectories()
        val resourceHtmlFile = resourceDir.resolve(srcFile.name).toFile()
        if (resourceHtmlFile.exists()) {
            resourceHtmlFile.delete()
        }
        resourceHtmlFile.createNewFile()
        return resourceHtmlFile
    }

    private fun getRootDirAndPackagePath(srcFile: File, messager: Messager?): Pair<String, String> {
        val seperator = FileSystems.getDefault().separator
        val srcMain = "src${seperator}main${seperator}"
        val list = if (srcFile.path.contains("${srcMain}kotlin")) {
            srcFile.path.split("${srcMain}kotlin")
        } else if (srcFile.path.contains("${srcMain}java")) {
            srcFile.path.split(("${srcMain}java"))
        } else {
            messager?.printMessage(Diagnostic.Kind.ERROR, "No src main found")
            throw ViewComponentProcessingException("No src main found", null)
        }
        return list[0] to list[1].split(srcFile.name)[0]
    }

    fun parseSrcHtmlFile(): List<String> = srcFile.readLines().map { htmlLine ->
            if (htmlLine.contains("<body")) {
                return@map htmlLine.replace("<body", "<body id=\"$viewComponentName\"")
            }
            if (htmlLine.contains("view:action")) {
                val newLine = htmlLine.parseViewActionHtmlLine()
                return@map newLine
            }
            htmlLine
        }

    private fun String.parseViewActionHtmlLine(): String {

        val beforeViewAction = this.split("view:action=\"")[0]
        val viewActionAttrValue = this.split("view:action=\"")[1].split("\"")[0]
        val afterViewActionAttrVal = this.split("view:action=\"")[1].split("\"")[1]
        val methodName = if (viewActionAttrValue.contains("?")) {
            if (viewActionAttrValue.startsWith("|")) {
                viewActionAttrValue.substring(1, viewActionAttrValue.indexOf("?"))
            } else {
                viewActionAttrValue.substring(0, viewActionAttrValue.indexOf("?"))
            }
        } else {
            viewActionAttrValue
        }
        val method = methodList.find { it.name.lowercase() == methodName.lowercase() }
            ?: let {
                messager?.printMessage(Diagnostic.Kind.ERROR, "viewAction method $methodName does not exist")
                throw ViewComponentProcessingException("viewAction method $methodName does not exist", null)
            }

        val (htmxAttr, path) = getHtmxAttribute(viewComponentName, method, messager)
        val htmxAttrVal = viewActionAttrValue.replace(methodName, path, true)
        return "$beforeViewAction$htmxAttr\"$htmxAttrVal\" hx-target=\"#$viewComponentName\"$afterViewActionAttrVal"
    }


    private fun getHtmxAttribute(
        viewComponentName: String,
        method: ViewActionMethod,
        messager: Messager?
    ): Pair<String, String> {
        val action = if (method.path == "") "/$viewComponentName/${method.name}" else method.path
        return when (method.clazz) {
            GetViewAction::class.java -> Pair("hx-get=", action.lowercase())
            PostViewAction::class.java -> Pair("hx-post=", action.lowercase())
            PutViewAction::class.java -> Pair("hx-put=", action.lowercase())
            PatchViewAction::class.java -> Pair("hx-patch=", action.lowercase())
            DeleteViewAction::class.java -> Pair("hx-delete=", action.lowercase())
            else -> let {
                messager?.printMessage(Diagnostic.Kind.ERROR, "No annotation found on $viewComponentName")
                throw ViewComponentProcessingException("No annotation found on $viewComponentName", null)
            }
        }
    }
}