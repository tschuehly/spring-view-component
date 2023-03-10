package de.tschuehly.thymeleafviewcomponent

import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class ViewComponentMvcConfigurer : WebMvcConfigurer {

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        handlers.add(ViewComponentMethodReturnValueHandler())
        super.addReturnValueHandlers(handlers)
    }
}
