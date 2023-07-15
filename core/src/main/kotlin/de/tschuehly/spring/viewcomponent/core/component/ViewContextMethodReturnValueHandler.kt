package de.tschuehly.spring.viewcomponent.core.component

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.CapturingResponseWrapper
import de.tschuehly.spring.viewcomponent.core.toMap
import jakarta.servlet.http.HttpServletResponseWrapper
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class ViewContextMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return IViewContext::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        val viewContext = returnValue?.takeIf {
            (IViewContext::class.java.isAssignableFrom(it.javaClass))
        } as IViewContext
        mavContainer.view = viewContext.componentTemplate
        mavContainer.addAllAttributes(viewContext.contextAttributes.toMap())
        val response = webRequest.nativeResponse
        if (response is HttpServletResponseWrapper) {
            setResponseBean(response,viewContext)
        }
    }


    private fun setResponseBean(wrapper: HttpServletResponseWrapper, viewContext: IViewContext) {
        if (wrapper is CapturingResponseWrapper) {
            wrapper.viewComponentBean = viewContext.componentBean
            return
        }
        val response = wrapper.response
        if(response is HttpServletResponseWrapper){
            setResponseBean(response, viewContext)
        }
    }
}