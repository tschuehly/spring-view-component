package de.tschuehly.spring.viewcomponent.jte
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class JteConfiguration {

    @Bean
    fun jteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): JteViewContextAspect {
        return JteViewContextAspect(templateEngine, jteProperties)
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine::class)
    fun jteTemplateEngine(viewComponentProperties: ViewComponentProperties,applicationContext: ApplicationContext): TemplateEngine {
        if (!viewComponentProperties.localDevelopment) {
            return TemplateEngine.createPrecompiled(
                /* classDirectory = */ Path.of(this.javaClass.classLoader.getResource("").toURI()),
                /* contentType = */ ContentType.Html,
                /* parentClassLoader = */ this.javaClass.classLoader
            )
        }
        val split = viewComponentProperties.viewComponentRoot.split("/").toTypedArray()
        val codeResolver: CodeResolver = DirectoryCodeResolver(FileSystems.getDefault().getPath("", *split))
        return TemplateEngine.create(
            codeResolver,
            Paths.get("jte-classes"),
            ContentType.Html,
            Thread.currentThread().contextClassLoader
        )
    }

    @Bean
    @Primary
    fun jteProperties(viewComponentProperties: ViewComponentProperties) = JteProperties().also {
        it.templateSuffix = ".jte"
        it.templateLocation = viewComponentProperties.viewComponentRoot
        it.isDevelopmentMode = viewComponentProperties.localDevelopment
    }
}