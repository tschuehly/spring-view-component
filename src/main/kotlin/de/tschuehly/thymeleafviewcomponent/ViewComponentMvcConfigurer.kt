package de.tschuehly.thymeleafviewcomponent

import org.springframework.format.FormatterRegistry
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.thymeleaf.spring6.SpringTemplateEngine

@Component
class ViewComponentMvcConfigurer(
    val springTemplateEngine: SpringTemplateEngine
) : WebMvcConfigurer{

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addFormatter(ViewComponentFormatter(springTemplateEngine))
        super.addFormatters(registry)
    }
    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        handlers.add(ViewComponentMethodReturnValueHandler())
        super.addReturnValueHandlers(handlers)
    }
}
