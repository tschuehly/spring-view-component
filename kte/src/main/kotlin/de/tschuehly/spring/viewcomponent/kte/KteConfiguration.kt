package de.tschuehly.spring.viewcomponent.kte

import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class KteConfiguration : BeanClassLoaderAware {
    var classLoader: ClassLoader? = null;

    @Bean
    fun kteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): KteViewContextAspect {
        return KteViewContextAspect(templateEngine, jteProperties)
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine::class)
    fun jteTemplateEngine(viewComponentProperties: ViewComponentProperties,applicationContext: ApplicationContext): TemplateEngine {
        if (!viewComponentProperties.localDevelopment) {
            return TemplateEngine.createPrecompiled(
                /* classDirectory = */ Path.of(classLoader?.getResource("")?.toURI() ?: throw RuntimeException("ClassLoader is null")),
                /* contentType = */ ContentType.Html,
                /* parentClassLoader = */ classLoader
            )
        }
        val split = viewComponentProperties.viewComponentRoot.split("/").toTypedArray()
        val codeResolver: CodeResolver = DirectoryCodeResolver(FileSystems.getDefault().getPath("", *split))
        return TemplateEngine.create(
            codeResolver,
            Paths.get("jte-classes"),
            ContentType.Html,
            classLoader
        )
    }

    @Bean
    @Primary
    fun jteProperties(viewComponentProperties: ViewComponentProperties) =
        JteProperties().also {
            it.templateSuffix = ".kte"
            it.templateLocation = viewComponentProperties.viewComponentRoot
            it.isDevelopmentMode = viewComponentProperties.localDevelopment
        }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }
}