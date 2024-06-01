package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import gg.jte.TemplateEngine
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ViewComponentAutoConfiguration::class)
class JteViewComponentAutoConfiguration {
    @Bean
    fun jteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): JteViewContextAspect {
        return JteViewContextAspect(templateEngine, jteProperties)
    }
}