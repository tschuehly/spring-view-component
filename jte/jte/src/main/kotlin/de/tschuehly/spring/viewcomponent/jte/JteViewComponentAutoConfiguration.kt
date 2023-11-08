package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import de.tschuehly.spring.viewcomponent.core.processor.ViewComponentParser
import gg.jte.ContentType
import gg.jte.TemplateEngine
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.support.AbstractApplicationContext
import java.nio.file.Paths

@Configuration
@Import(ViewComponentAutoConfiguration::class)
@EnableConfigurationProperties(ViewComponentProperties::class)
class JteViewComponentAutoConfiguration(
    private val viewComponentProperties: ViewComponentProperties
) {
    @Bean
    fun jteViewContextAspect(templateEngine: TemplateEngine): JteViewContextAspect {
        return JteViewContextAspect(templateEngine, viewComponentProperties)
    }

    @Bean
    fun jteTemplateEngine(viewComponentCodeResolver: ViewComponentCodeResolver): TemplateEngine {
        if (viewComponentProperties.localDevelopment) {
            return TemplateEngine.create(
                viewComponentCodeResolver,
                Paths.get("jte-classes"),
                ContentType.Html,
                this::class.java.classLoader
            )
        }
        return TemplateEngine.createPrecompiled(
            null,
            ContentType.Html,
            null,
            "de"
        );

    }

    @Bean
    fun viewComponentCodeResolver(applicationContext: ApplicationContext): ViewComponentCodeResolver {
        return ViewComponentCodeResolver(
            applicationContext,
            ViewComponentParser.BuildType.GRADLE,
            Paths.get("src/main/java")
        )
    }

}