package de.tschuehly.spring.viewcomponent.kte

import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.springframework.boot.autoconfigure.JteProperties
import gg.jte.springframework.boot.autoconfigure.JteViewResolver
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class KteConfiguration : BeanClassLoaderAware {
    var classLoader: ClassLoader? = null

    @Bean
    fun kteViewContextAspect(
        templateEngine: TemplateEngine,
        jteProperties: JteProperties
    ): KteViewContextAspect {
        return KteViewContextAspect(templateEngine, jteProperties)
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine::class)
    fun kteNonLocalTemplateEngine(viewComponentProperties: ViewComponentProperties): TemplateEngine {
        if (viewComponentProperties.localDevelopment) {
            val split = getViewComponentRootPath(viewComponentProperties).split("/")
                .toTypedArray()
            val codeResolver: CodeResolver =
                DirectoryCodeResolver(FileSystems.getDefault().getPath("", *split))
            return TemplateEngine.create(
                codeResolver,
                Paths.get("jte-classes"),
                ContentType.Html,
                classLoader
            )
        }
        return TemplateEngine.createPrecompiled(
            /* classDirectory = */ Path.of(
                classLoader?.getResource("")?.toURI()
                    ?: throw RuntimeException("ClassLoader is null")
            ),
            /* contentType = */ ContentType.Html,
            /* parentClassLoader = */ classLoader
        )
    }

    @Bean
    fun kteViewResolver(templateEngine: TemplateEngine?, jteProperties: JteProperties): JteViewResolver {
        return JteViewResolver(templateEngine, jteProperties)
    }

    @Bean
    @Primary
    fun jteProperties(viewComponentProperties: ViewComponentProperties) =
        JteProperties().also {
            it.templateSuffix = ".kte"
            it.templateLocation = getViewComponentRootPath(viewComponentProperties)
            it.isDevelopmentMode = viewComponentProperties.localDevelopment
        }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    private fun getViewComponentRootPath(viewComponentProperties: ViewComponentProperties): String =
        if (viewComponentProperties.viewComponentRoot == "src/main/java") "src/main/kotlin" else viewComponentProperties.viewComponentRoot
}