package de.tschuehly.thymeleafviewcomponent

import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.ModelAndViewMethodReturnValueHandler

@Component
class ViewComponentReturnValueConfiguration : WebMvcConfigurer{
    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        handlers.add(ViewComponentMethodReturnValueHandler())
        super.addReturnValueHandlers(handlers)
    }
}

class ViewComponentMethodReturnValueHandler(): ModelAndViewMethodReturnValueHandler() {
    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return ViewComponentContext::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        val viewComponentContext = returnValue as ViewComponentContext
        if(viewComponentContext.componentClass == null){
            throw Error("Could not get the ViewComponent Class")
        }
        val componentName = viewComponentContext.componentClass.simpleName.substringBefore("$$")
        val componentPackage = viewComponentContext.componentClass.`package`.name.replace(".","/") + "/"
        mavContainer.view = "$componentPackage$componentName.html"
        mavContainer.addAllAttributes(viewComponentContext.context)
//        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest)
    }

}