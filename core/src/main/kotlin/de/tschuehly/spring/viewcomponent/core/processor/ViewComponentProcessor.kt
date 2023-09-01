package de.tschuehly.spring.viewcomponent.core.processor

import de.tschuehly.spring.viewcomponent.core.action.*
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentParser.BuildType
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists


@SupportedAnnotationTypes("de.tschuehly.spring.viewcomponent.core.component.ViewComponent")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
class ViewComponentProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        for (annotation in annotations) {
            for (element in roundEnv.getElementsAnnotatedWith(annotation)) {
                val messager = processingEnv.messager
                val (rootDir, buildType) = processingEnv.getRootDirAndBuildType()

                val separator = FileSystems.getDefault().separator
                val packagePath = "${element.enclosingElement}".replace(".", separator)
                val srcDir = getSrcDir(rootDir, messager)
                val viewComponentDir = FileSystems.getDefault().getPath(srcDir.toString(), packagePath)
                messager.printMessage(Diagnostic.Kind.NOTE, "SrcDirPath: " + viewComponentDir.absolutePathString())

                val srcHtmlFile = getSrcHtmlFile(viewComponentDir, element.simpleName, messager)
                val methodList = getViewActionMethods(element)

                val viewComponentName = element.simpleName.toString().lowercase()
                verifyRenderMethodExists(element, messager)

                val viewComponentParser = ViewComponentParser(
                    srcHtmlFile,
                    buildType = buildType,
                    methodList = methodList,
                    viewComponentName = viewComponentName,
                    messager = messager,
                )
                viewComponentParser.parseFile(false)
            }
        }
        return true
    }


    private fun getSrcDir(rootDir: String, messager: Messager?): Path {
        val mainPath = FileSystems.getDefault()
            .getPath(rootDir, "src", "main")
        if (mainPath.resolve("kotlin").exists()) {
            return mainPath.resolve("kotlin")
        }
        if (mainPath.resolve("java").exists()) {
            return mainPath.resolve("java")
        }
        messager?.printMessage(Diagnostic.Kind.ERROR, "No src main found")
        throw ViewComponentProcessingException("$mainPath/src/main/[java,kotlin] not found", null)
    }

    private fun ProcessingEnvironment.getJavaRootDir(): String {
        try {
            val sourceFile = this.filer.createClassFile("gen_${Date().toInstant().toEpochMilli()}.java")
            val srcDirPath = Paths.get(sourceFile.toUri()).toString()
            return srcDirPath
        } catch (e: IOException) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Unable to determine source file path!")
        }
        this.messager.printMessage(Diagnostic.Kind.ERROR, "Could not get create class file")
        throw ViewComponentProcessingException("Could not get create class file", null)
    }

    private fun ProcessingEnvironment.getKotlinRootDir(): String? {
        this.options["kapt.kotlin.generated"]?.let { filePath ->
            return filePath
        }
        return null
    }

    private fun ProcessingEnvironment.getRootDirAndBuildType(): Pair<String, BuildType> {
        val filePath = this.getKotlinRootDir() ?: this.getJavaRootDir()
        if (filePath.contains("target")) {
            return filePath.split("target")[0] to BuildType.MAVEN
        }
        if (filePath.contains("build")) {
            return filePath.split("build")[0] to BuildType.GRADLE
        }
        this.messager.printMessage(Diagnostic.Kind.ERROR, "No build or target folder found")
        throw ViewComponentProcessingException("No build or target folder found", null)

    }

    private fun verifyRenderMethodExists(element: Element, messager: Messager) {
        val renderReturnType = element.enclosedElements.filter { it.kind == ElementKind.METHOD }
            .find { it.simpleName.toString() == "render" }
            ?.asType()
            ?: let {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "You need to define a render method in the $element that returns a ViewContext"
                )
                throw ViewComponentProcessingException(
                    "You need to define a render method in the $element that returns a ViewContext",
                    null
                )
            }

        val viewProperties = ((element.enclosedElements.filter { it.kind == ElementKind.METHOD }
            .find { it.simpleName.toString() == "render" })?.asType().toString().substring(2)
            .let { name -> element.enclosedElements.find { it.toString() == name } })
            ?.enclosedElements?.filter { it.kind == ElementKind.FIELD }
    }

    private fun getSrcHtmlFile(srcDirPath: Path, viewComponentName: Name, messager: Messager): File {
        val fileEndings = listOf(".html", ".jte", ".kte", ".th")
        fileEndings.forEach { fileEnding ->
            val file = srcDirPath.absolute().resolve("$viewComponentName$fileEnding").toFile()
            if (file.exists()) {
                messager.printMessage(Diagnostic.Kind.NOTE, "Found file at ${file.path}")
                return file
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "Didn't find file at ${file.path}")
        }
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Couldn't find a file at $srcDirPath"
        )
        throw ViewComponentProcessingException("Couldn't find a file at $srcDirPath", null)
    }

    private fun getViewActionMethods(element: Element): List<ViewActionMethod> {
        val methodList = element.enclosedElements.filter { it.kind == ElementKind.METHOD }.mapNotNull { method ->
            if (method.getAnnotation(GetViewAction::class.java) != null) {
                val get = (method.getAnnotation(GetViewAction::class.java) as GetViewAction)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), get.path, GetViewAction::class.java)
            }
            if (method.getAnnotation(PostViewAction::class.java) != null) {
                val get = (method.getAnnotation(PostViewAction::class.java) as PostViewAction)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), get.path, PostViewAction::class.java)
            }
            if (method.getAnnotation(PutViewAction::class.java) != null) {
                val get = (method.getAnnotation(PutViewAction::class.java) as PutViewAction)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), get.path, PutViewAction::class.java)
            }
            if (method.getAnnotation(PatchViewAction::class.java) != null) {
                val get = (method.getAnnotation(PatchViewAction::class.java) as PatchViewAction)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), get.path, PatchViewAction::class.java)
            }
            if (method.getAnnotation(DeleteViewAction::class.java) != null) {
                val get = (method.getAnnotation(DeleteViewAction::class.java) as DeleteViewAction)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), get.path, DeleteViewAction::class.java)
            }
            return@mapNotNull null
        }
        return methodList
    }


}
