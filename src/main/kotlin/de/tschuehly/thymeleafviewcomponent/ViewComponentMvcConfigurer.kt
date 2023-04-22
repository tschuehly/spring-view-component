package de.tschuehly.thymeleafviewcomponent

import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class ViewComponentMvcConfigurer(
    vararg val methodReturnValueHandler: HandlerMethodReturnValueHandler,
) : WebMvcConfigurer {

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        methodReturnValueHandler.forEach {
            handlers.add(it)
        }
        handlers.add(ViewContextAsyncHandlerMethodReturnValueHandler())
        handlers.add(ViewContextMethodReturnValueHandler())
        super.addReturnValueHandlers(handlers)
    }
}
