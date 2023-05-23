package de.tschuehly.spring.viewcomponent.core

import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class ViewComponentMvcConfigurer(
    private vararg val methodReturnValueHandler: HandlerMethodReturnValueHandler,
    private val viewActionParseInterceptor: ViewActionParseInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(viewActionParseInterceptor)
    }

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        methodReturnValueHandler.forEach {
            handlers.add(it)
        }
        super.addReturnValueHandlers(handlers)
    }
}
