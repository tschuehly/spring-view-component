package de.tschuehly.thymeleafviewcomponent

import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class ViewComponentMvcConfigurer(
    private val viewContextContainerMethodReturnValueHandler: ViewContextContainerMethodReturnValueHandler,
) : WebMvcConfigurer {

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        handlers.add(ViewContextAsyncHandlerMethodReturnValueHandler())
        handlers.add(viewContextContainerMethodReturnValueHandler)
        handlers.add(ViewContextMethodReturnValueHandler())
        super.addReturnValueHandlers(handlers)
    }
}
