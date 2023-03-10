package de.tschuehly.thymeleafviewcomponent

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.thymeleaf.dialect.IDialect
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver


@Configuration
@ComponentScan("de.tschuehly.thymeleafviewcomponent")
class ViewComponentAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(SpringTemplateEngine::class)
    fun templateEngine(
        properties: ThymeleafProperties,
        templateResolvers: ObjectProvider<ITemplateResolver>, dialects: ObjectProvider<IDialect>
    ): SpringTemplateEngine? {
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
        engine.addDialect(ViewComponentDialect())
        return engine
    }

    @Bean
    fun secondaryTemplateResolver(): ClassLoaderTemplateResolver? {
        val secondaryTemplateResolver = ClassLoaderTemplateResolver()
        secondaryTemplateResolver.prefix = ""
        secondaryTemplateResolver.suffix = ".html"
        secondaryTemplateResolver.templateMode = TemplateMode.HTML
        secondaryTemplateResolver.characterEncoding = "UTF-8"
        secondaryTemplateResolver.order = 1
        secondaryTemplateResolver.checkExistence = true
        return secondaryTemplateResolver
    }
}