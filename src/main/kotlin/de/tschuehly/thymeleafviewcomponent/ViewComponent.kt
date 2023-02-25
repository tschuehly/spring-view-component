package de.tschuehly.thymeleafviewcomponent

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ViewComponent()

@Aspect
@Component
class ViewComponentAspect(
) {
    @Around("execution(* render()) || execution(* render(*))")
    fun renderInject(joinPoint: ProceedingJoinPoint): ViewComponentContext {
        val returnValue = joinPoint.proceed()
        if(returnValue !is ViewComponentContext){
            throw Error("render method needs to return a ViewComponentContext")
        }
        val componentName = joinPoint.`this`.javaClass.simpleName.substringBefore("$$")
        val componentPackage = joinPoint.`this`.javaClass.`package`.name.replace(".","/") + "/"
        return ViewComponentContext(returnValue.context, "$componentPackage$componentName.html")
    }
}