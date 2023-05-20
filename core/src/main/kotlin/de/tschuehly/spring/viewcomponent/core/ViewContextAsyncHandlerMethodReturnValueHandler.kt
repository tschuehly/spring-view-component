package de.tschuehly.spring.viewcomponent.core

import org.springframework.core.MethodParameter
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import reactor.core.publisher.Mono

@Component
class ViewContextAsyncHandlerMethodReturnValueHandler() : AsyncHandlerMethodReturnValueHandler {
    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return Mono::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        if(returnValue == null){
            throw Exception("Return Value is null")
        }
        val mono= returnValue.takeIf {
            (Mono::class.java.isAssignableFrom(it.javaClass))
        } as Mono<*>
        val responseEntity = mono.block()?.takeIf {
            ResponseEntity::class.java.isAssignableFrom(it.javaClass)
        } as ResponseEntity<*>
        val viewContext = responseEntity.body?.takeIf {
            IViewContext::class.java.isAssignableFrom(it.javaClass)
        } as IViewContext
        mavContainer.view = viewContext.componentTemplate
        mavContainer.addAllAttributes(viewContext.contextAttributes.toMap())
    }

    override fun isAsyncReturnValue(returnValue: Any?, returnType: MethodParameter): Boolean {
        return returnValue != null && supportsReturnType(returnType)
    }
}