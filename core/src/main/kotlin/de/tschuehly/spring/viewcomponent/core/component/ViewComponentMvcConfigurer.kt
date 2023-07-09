package de.tschuehly.spring.viewcomponent.core.component

import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class ViewComponentMvcConfigurer(
    private val methodReturnValueHandlers: List<HandlerMethodReturnValueHandler>,
) : WebMvcConfigurer {

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        methodReturnValueHandlers.forEach {
            handlers.add(it)
        }
        super.addReturnValueHandlers(handlers)
    }
}
