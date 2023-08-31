package de.tschuehly.spring.viewcomponent.core.processor

import de.tschuehly.spring.viewcomponent.core.action.*
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

    fun parseFile() {
        val parsedHtml = parseSrcHtmlFile()
        val (rootDir, packagePath) = getRootDirAndPackagePath(srcFile, messager)
        val resourceDirPath = getResourceDirPath(rootDir, packagePath)
        val resourceHtmlFile = getResourceFile(resourceDirPath)
        resourceHtmlFile.writeAll(parsedHtml)

        try {
            val clazz = Class.forName("de.tschuehly.spring.viewcomponent.jte.JteViewComponentCompiler")
            val compiler = (clazz.getConstructor().newInstance() as ViewComponentCompiler)

            val classDir = getClassDirPath(rootDir)
            compiler.compile(
                rootDir = resourceDirPath.toAbsolutePath(),
                names = srcFile.name,
                classDirectory = listOf(
                    classDir.toAbsolutePath().toString()
                )
            )
        } catch (e: ClassNotFoundException) {
            messager?.printMessage(
                Diagnostic.Kind.NOTE,
                "If you don't use JTE this message can be ignored: JteViewComponentCompiler not found"
            )
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

    private fun getClassDirPath(rootDir: String): Path {
        return if (buildType == BuildType.GRADLE) {
            FileSystems.getDefault()
                .getPath(rootDir, "build", "classes", "kotlin", "main")
        } else {
            FileSystems.getDefault()
                .getPath(rootDir, "target", "classes")
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

    private fun parseSrcHtmlFile(): List<String> = srcFile.readLines().map { htmlLine ->
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