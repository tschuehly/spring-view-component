package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.core.ViewComponentAutoConfiguration
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.thymeleaf.dialect.IDialect
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.File


@Configuration
@Import(ViewComponentAutoConfiguration::class)
@EnableConfigurationProperties(ViewComponentProperties::class)
class ThymeleafViewComponentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SpringTemplateEngine::class)
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
    @ConditionalOnProperty("spring.view-component.local-development")
    fun fileViewComponentTemplateResolver(): FileTemplateResolver {
        val fileViewComponentTemplateResolver = FileTemplateResolver()
        if (File("src/main/kotlin").isDirectory) {
            fileViewComponentTemplateResolver.prefix = "src/main/kotlin/"
        }
        if (File("src/main/java").isDirectory) {
            fileViewComponentTemplateResolver.prefix = "src/main/java/"
        }
        fileViewComponentTemplateResolver.suffix = ".html"
        fileViewComponentTemplateResolver.templateMode = TemplateMode.HTML
        fileViewComponentTemplateResolver.isCacheable = false
        fileViewComponentTemplateResolver.characterEncoding = "UTF-8"
        fileViewComponentTemplateResolver.order = 1
        fileViewComponentTemplateResolver.checkExistence = true
        return fileViewComponentTemplateResolver
    }

    @Bean
    fun viewComponentTemplateResolver(): ClassLoaderTemplateResolver {
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.prefix = ""
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding = "UTF-8"
        templateResolver.order = 1
        templateResolver.checkExistence = true
        return templateResolver
    }


}



