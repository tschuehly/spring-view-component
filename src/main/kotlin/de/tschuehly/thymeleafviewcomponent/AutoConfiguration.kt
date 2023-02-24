package de.tschuehly.thymeleafviewcomponent

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver


@Configuration
@ComponentScan("de.tschuehly.thymeleafviewcomponent")
class AutoConfiguration(
    val applicationContext: ApplicationContext
) {

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


//    @Bean
//    fun viewComponentResolver(): ViewComponentResolver{
//        val viewComponentResolver =  ViewComponentResolver(2)
//        return viewComponentResolver
//    }

}