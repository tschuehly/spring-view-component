package de.tschuehly.spring.viewcomponent.thymeleaf

import de.tschuehly.spring.viewcomponent.common.ViewContextContainer
import de.tschuehly.spring.viewcomponent.common.toMap
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.support.WebApplicationObjectSupport
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import org.thymeleaf.spring6.view.ThymeleafView
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import java.util.*

class ThymeleafViewContextContainerMethodReturnValueHandler(
    private val thymeleafViewResolver: ThymeleafViewResolver
) : HandlerMethodReturnValueHandler, WebApplicationObjectSupport() {

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return ViewContextContainer::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)!!
        val response = webRequest.getNativeResponse(HttpServletResponse::class.java)!!
        val viewContextContainer = returnValue as ViewContextContainer
        viewContextContainer.viewContexts.forEach { viewContext ->
            val view: ThymeleafView =
                thymeleafViewResolver.resolveViewName(viewContext.componentTemplate!!, Locale.GERMAN) as ThymeleafView
            view.render(viewContext.contextAttributes.toMap(), request, response)
        }
        mavContainer.isRequestHandled = true
    }

}


