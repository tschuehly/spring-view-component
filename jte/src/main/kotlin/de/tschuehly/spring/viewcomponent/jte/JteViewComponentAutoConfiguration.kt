package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateConfig
import gg.jte.TemplateEngine
import gg.jte.compiler.TemplateCompiler
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.runtime.Constants
import gg.jte.runtime.TemplateMode
import gg.jte.springframework.boot.autoconfigure.JteConfigurationException
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

@Configuration

@PropertySource("jte.properties")
@Import(ViewComponentAutoConfiguration::class)
class JteViewComponentAutoConfiguration {
    @Bean
    fun jteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): JteViewContextAspect {
        return JteViewContextAspect(templateEngine, jteProperties)
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine::class)
    fun jteTemplateEngine(viewComponentProperties: ViewComponentProperties,applicationContext: ApplicationContext): TemplateEngine {
        if (!viewComponentProperties.localDevelopment) {

            val packageList = applicationContext.getBeansWithAnnotation(ViewComponent::class.java).values.map {
                it.javaClass.packageName
            }

            val config = TemplateConfig(
                ContentType.Html,
                Constants.PACKAGE_NAME_PRECOMPILED
            )
            config.classPath = null
            val compiler = TemplateCompiler(
                /* config = */ config,
                /* codeResolver = */ DirectoryCodeResolver(Path.of("")),
                /* classDirectory = */ Path.of("src/main/java"),
                /* parentClassLoader = */ this.javaClass.classLoader
            )
            compiler.generateAll()
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(
                /* classDirectory = */ null,
                /* contentType = */ ContentType.Html,
                /* parentClassLoader = */ null,
                /* packageName = */ ""
            )
        }
        val split = viewComponentProperties.viewComponentRoot.split("/").toTypedArray()
        val codeResolver: CodeResolver = DirectoryCodeResolver(FileSystems.getDefault().getPath("", *split))
        return TemplateEngine.create(
            codeResolver,
            Paths.get("jte-classes"),
            ContentType.Html,
            javaClass.classLoader
        )
    }


    @Bean
    @Primary
    fun jteProperties(viewComponentProperties: ViewComponentProperties) = JteProperties().also {
        it.templateSuffix = viewComponentProperties.templateSuffix
        it.templateLocation = viewComponentProperties.viewComponentRoot
        it.isDevelopmentMode = viewComponentProperties.localDevelopment
    }
}