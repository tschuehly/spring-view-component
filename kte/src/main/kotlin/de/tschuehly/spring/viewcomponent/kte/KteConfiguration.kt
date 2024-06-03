package de.tschuehly.spring.viewcomponent.kte
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class KteConfiguration {

    @Bean
    fun kteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): KteViewContextAspect {
        return KteViewContextAspect(templateEngine, jteProperties)
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
        val split = getViewComponentRootPath(viewComponentProperties).split("/").toTypedArray()
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
        it.templateSuffix = ".kte"
        it.templateLocation = getViewComponentRootPath(viewComponentProperties)
        it.isDevelopmentMode = viewComponentProperties.localDevelopment
    }

    private fun getViewComponentRootPath(viewComponentProperties: ViewComponentProperties): String =
        if (viewComponentProperties.viewComponentRoot == "src/main/java") "src/main/kotlin" else viewComponentProperties.viewComponentRoot


}