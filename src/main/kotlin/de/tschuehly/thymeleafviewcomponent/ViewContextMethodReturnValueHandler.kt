package de.tschuehly.thymeleafviewcomponent

import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer


class ViewContextMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return ViewContext::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        val viewContext = returnValue?.takeIf {
            (ViewContext::class.java.isAssignableFrom(it.javaClass))
        } as ViewContext
        mavContainer.view = viewContext.componentTemplate
        mavContainer.addAllAttributes(viewContext.contextAttributes.toMap())
    }

}