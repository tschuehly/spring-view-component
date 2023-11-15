package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentChangeListener
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentParser
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentProcessingException
import gg.jte.CodeResolver
import gg.jte.TemplateNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.context.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.nameWithoutExtension


class ViewComponentCodeResolver(
    private val applicationContext: ApplicationContext,
    private val templateDirectories: List<String>
) : CodeResolver {
    private val logger = LoggerFactory.getLogger(ViewComponentCodeResolver::class.java)
    override fun resolve(name: String): String? {
        return try {
            resolveRequired(name)
        } catch (e: TemplateNotFoundException) {
            null
        }
    }

    override fun resolveRequired(name: String): String {
        val path = resolveFile(name)
        try {

            val bean = applicationContext.getBeansWithAnnotation(ViewComponent::class.java).filter {
                it.key.lowercase() == path.nameWithoutExtension.lowercase()
            }.values.first()
            val javaClass = if (AopUtils.isAopProxy(bean) && bean is Advised) {
                bean.targetSource.target!!.javaClass
            } else bean.javaClass
            val buildType = getBuildType(javaClass.protectionDomain.codeSource.location.path)
            val methodList = ViewComponentChangeListener.getViewActionMethods(javaClass)
            val parser = ViewComponentParser(
                srcFile = path,
                buildType = buildType,
                methodList = methodList,
                viewComponentName = javaClass.simpleName.lowercase()
            )
            return parser.parseSrcHtmlFile().joinToString("\n")
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    private fun resolveFile(name: String): File {
        val files = templateDirectories.map {
            Paths.get(it).resolve(name).toFile().absoluteFile
        }
        try{
            return files.first { it.exists() }

        }catch (e: NoSuchElementException){
            val message = "$name not found (tried to load file at ${files}})"
            logger.debug(message)
            throw TemplateNotFoundException(message)

        }

    }

    private fun getBuildType(filePath: String): ViewComponentParser.BuildType {
        if (filePath.contains("target")) {
            return ViewComponentParser.BuildType.MAVEN
        }
        if (filePath.contains("build")) {
            return ViewComponentParser.BuildType.GRADLE
        }
        throw ViewComponentProcessingException("No build or target folder found", null)
    }

    override fun getLastModified(name: String): Long {
        return getLastModified(resolveFile(name))
    }

    private fun getLastModified(file: File): Long {
        return file.lastModified()
    }

}