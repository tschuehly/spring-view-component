package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentChangeListener
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentParser
import gg.jte.CodeResolver
import gg.jte.TemplateNotFoundException
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.context.ApplicationContext
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension


class ViewComponentCodeResolver(
    private val applicationContext: ApplicationContext,
    private val buildType: ViewComponentParser.BuildType,
    private val root: Path): CodeResolver {
    override fun resolve(name: String): String? {
        return try {
            resolveRequired(name)
        } catch (e: TemplateNotFoundException) {
            null
        }
    }

    override fun resolveRequired(name: String): String {
        val path: Path = root.resolve(name)
        try {

            val bean = applicationContext.getBeansWithAnnotation(ViewComponent::class.java).filter {
                it.key.lowercase() == path.nameWithoutExtension.lowercase()
            }.values.first()
            val javaClass = if (AopUtils.isAopProxy(bean) && bean is Advised) {
                bean.targetSource.target!!.javaClass
            } else bean.javaClass
            val methodList = ViewComponentChangeListener.getViewActionMethods(javaClass)
            val parser = ViewComponentParser(
                srcFile = path.toFile(),
                buildType = buildType,
                methodList = methodList,
                viewComponentName = javaClass.simpleName.lowercase()
            )
            return parser.parseSrcHtmlFile().joinToString ("\n")
        } catch (e: NoSuchFileException) {
            throw TemplateNotFoundException(name + " not found (tried to load file at " + path.toAbsolutePath() + ")")
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun getLastModified(name: String): Long {
        return getLastModified(root.resolve(name))
    }
    private fun getLastModified(file: Path): Long {
        return file.toFile().lastModified()
    }

}