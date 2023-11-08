package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.runtime.TemplateMode
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
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
    fun jteTemplateEngine(): TemplateEngine {
        return TemplateEngine.create(
            null,
            Paths.get(""),
            ContentType.Html,
            null,
            ""
        )
    }
}