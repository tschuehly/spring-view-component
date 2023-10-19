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
import javax.lang.model.element.*
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.io.path.absolute
import kotlin.io.path.exists


@SupportedAnnotationTypes("de.tschuehly.spring.viewcomponent.core.component.ViewComponent")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
class ViewComponentProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        for (annotation in annotations) {
            for (element in roundEnv.getElementsAnnotatedWith(annotation)) {
                val messager = processingEnv.messager
                val filer = processingEnv.filer
                val (rootDir, buildType) = processingEnv.getRootDirAndBuildType()

                val separator = FileSystems.getDefault().separator
                val packagePath = "${element.enclosingElement}".replace(".", separator)
                val srcDir = getSrcDir(rootDir, messager)
                val viewComponentDir = FileSystems.getDefault().getPath(srcDir.toString(), packagePath)
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
                val generatedFile = viewComponentParser.parseFile(false)
                if (generatedFile != null) {
                    val fil = filer.getResource(
                        StandardLocation.SOURCE_OUTPUT,
                        generatedFile.substringBeforeLast("/").replace("/", "."),
                        generatedFile.substringAfterLast("/")
                    )
                    val generatedSourceText = fil.openReader(true).use {
                        it.readText()
                    }
                    val srcFile =
                        processingEnv.filer.createSourceFile(generatedFile.replace("/", ".").replace(".java", ""))
                    srcFile.openWriter().use {
                        it.write(generatedSourceText)
                        it.flush()
                    }
                }

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
            val fileName = "gen_${Date().toInstant().toEpochMilli()}"
            val sourceFile = this.filer.createSourceFile(fileName)

            sourceFile.openWriter().use {
                it.close()
            }


            return Paths.get(sourceFile.toUri()).let {
//                it.deleteExisting() TODO: Cannot delete
                it.toString()
            }
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
        val testedFiles = mutableListOf<String>()
        fileEndings.forEach { fileEnding ->
            val file = srcDirPath.absolute().resolve("$viewComponentName$fileEnding").toFile()
            if (file.exists()) {
                messager.printMessage(Diagnostic.Kind.NOTE, "Found ViewComponent Template at ${file.path}")
                return file
            }
            testedFiles.add(file.path)
        }
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Couldn't find a template for $viewComponentName tried at following paths: $testedFiles"
        )
        throw ViewComponentProcessingException("Couldn't find a file at $srcDirPath", null)
    }

    private fun getViewActionMethods(element: Element): List<ViewActionMethod> {
        val methodList = element.enclosedElements.filter { it.kind == ElementKind.METHOD }.mapNotNull { method ->
            if (method.getAnnotation(GetViewAction::class.java) != null) {
                val get = (method.getAnnotation(GetViewAction::class.java) as GetViewAction)
                checkIfMethodIsPublic(method, element)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), get.path, GetViewAction::class.java)
            }
            if (method.getAnnotation(PostViewAction::class.java) != null) {
                val post = (method.getAnnotation(PostViewAction::class.java) as PostViewAction)
                checkIfMethodIsPublic(method, element)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), post.path, PostViewAction::class.java)
            }
            if (method.getAnnotation(PutViewAction::class.java) != null) {
                val put = (method.getAnnotation(PutViewAction::class.java) as PutViewAction)
                checkIfMethodIsPublic(method, element)
                return@mapNotNull ViewActionMethod(method.simpleName.toString(), put.path, PutViewAction::class.java)
            }
            if (method.getAnnotation(PatchViewAction::class.java) != null) {
                val patch = (method.getAnnotation(PatchViewAction::class.java) as PatchViewAction)
                checkIfMethodIsPublic(method, element)
                return@mapNotNull ViewActionMethod(
                    method.simpleName.toString(),
                    patch.path,
                    PatchViewAction::class.java
                )
            }
            if (method.getAnnotation(DeleteViewAction::class.java) != null) {
                val delete = (method.getAnnotation(DeleteViewAction::class.java) as DeleteViewAction)
                checkIfMethodIsPublic(method, element)
                return@mapNotNull ViewActionMethod(
                    method.simpleName.toString(),
                    delete.path,
                    DeleteViewAction::class.java
                )
            }
            return@mapNotNull null
        }
        return methodList
    }

    private fun checkIfMethodIsPublic(method: Element, element: Element) {
        if (method.modifiers.contains(Modifier.PUBLIC) == false) {
            throw ViewComponentProcessingException(
                "Method: " + element.simpleName + "::" + method.simpleName + " needs to be public",
                null
            )
        }
    }


}
