package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.TemplateEngine
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource

@Configuration

@PropertySource("jte.properties")
@Import(ViewComponentAutoConfiguration::class)
class JteViewComponentAutoConfiguration {
    @Bean
    fun jteViewContextAspect(templateEngine: TemplateEngine, jteProperties: JteProperties): JteViewContextAspect {
        return JteViewContextAspect(templateEngine, jteProperties)
    }


    @Bean
    @Primary
    fun jteProperties(viewComponentProperties: ViewComponentProperties) = JteProperties().also {
        it.templateSuffix = viewComponentProperties.templateSuffix
        it.templateLocation = viewComponentProperties.viewComponentRoot
        it.isDevelopmentMode = viewComponentProperties.localDevelopment
        it.setUsePrecompiledTemplates(!viewComponentProperties.localDevelopment)
    }
}