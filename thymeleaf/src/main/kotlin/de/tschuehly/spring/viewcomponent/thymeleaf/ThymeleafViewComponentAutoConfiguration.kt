package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.thymeleaf.dialect.IDialect
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver


@Configuration
@Import(ViewComponentAutoConfiguration::class)
class ThymeleafViewComponentAutoConfiguration {

    @Bean
    fun templateEngine(
        properties: ThymeleafProperties,
        templateResolvers: ObjectProvider<ITemplateResolver>,
        dialects: ObjectProvider<IDialect>
    ): SpringTemplateEngine {
        val engine = SpringTemplateEngine()
        engine.enableSpringELCompiler = properties.isEnableSpringElCompiler
        engine.renderHiddenMarkersBeforeCheckboxes = properties.isRenderHiddenMarkersBeforeCheckboxes
        templateResolvers.orderedStream().forEach { templateResolver ->
            engine.addTemplateResolver(
                templateResolver
            )
        }
        dialects.orderedStream().forEach { dialect: IDialect? ->
            engine.addDialect(
                dialect
            )
        }
        engine.addDialect(ThymeleafViewComponentDialect())
        return engine
    }


    @Bean
    @ConditionalOnProperty("spring.view-component.local-development", havingValue = "true")
    fun fileViewComponentTemplateResolver(viewComponentProperties: ViewComponentProperties): FileTemplateResolver {
        return FileTemplateResolver().also {
            configureResolver(it)
            it.prefix = viewComponentProperties.viewComponentRoot + "/"
        }
    }


    @Bean
    @ConditionalOnProperty("spring.view-component.local-development", havingValue = "false")
    fun viewComponentTemplateResolver(viewComponentProperties: ViewComponentProperties): ClassLoaderTemplateResolver {
        return ClassLoaderTemplateResolver().also {
            configureResolver(it)
            it.prefix = ""
        }
    }

    private fun configureResolver(templateResolver: AbstractConfigurableTemplateResolver) {
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.isCacheable = false
        templateResolver.characterEncoding = "UTF-8"
        templateResolver.order = 1
        templateResolver.checkExistence = true
    }


}



