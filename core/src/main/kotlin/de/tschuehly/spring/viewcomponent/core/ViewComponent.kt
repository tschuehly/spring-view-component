package de.tschuehly.spring.viewcomponent.core

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ViewComponent

@Aspect
@Component
class ViewComponentAspect {
    @Pointcut("@within(de.tschuehly.spring.viewcomponent.core.ViewComponent)")
    fun isViewComponent(){
        //
    }

    @Pointcut("execution(* render(..)) || execution(* get(..))")
    fun isRenderOrGetMethod(){
        //
    }

    @Around("isViewComponent() && isRenderOrGetMethod()")
    fun renderInject(joinPoint: ProceedingJoinPoint): ViewContext {

        val returnValue = joinPoint.proceed()
        if (returnValue !is ViewContext) {
            throw Error("render method needs to return a ViewContext")
        }
        val componentName = joinPoint.`this`.javaClass.simpleName.substringBefore("$$")
        val componentPackage = joinPoint.`this`.javaClass.`package`.name.replace(".", "/") + "/"
        return ViewContext(
            componentTemplate = "$componentPackage$componentName",
            contextAttributes = returnValue.contextAttributes
        )
    }
}