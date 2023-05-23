package de.tschuehly.spring.viewcomponent.core

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import kotlin.reflect.full.isSubclassOf

@Aspect
@Component
class ViewComponentAspect {
    @Pointcut("@within(de.tschuehly.spring.viewcomponent.core.ViewComponent)")
    fun isViewComponent() {
        //
    }

    @Around("isViewComponent() && execution(* *(..))")
    fun renderInject(joinPoint: ProceedingJoinPoint): IViewContext {

        val returnValue = joinPoint.proceed()
        if (returnValue::class.isSubclassOf(IViewContext::class)) {
            returnValue as IViewContext
            val componentName = joinPoint.`this`.javaClass.simpleName.substringBefore("$$")
            val componentPackage = joinPoint.`this`.javaClass.`package`.name.replace(".", "/") + "/"
            returnValue.componentTemplate = "$componentPackage$componentName"
            returnValue.componentBean = joinPoint.target
            return returnValue
        }
        throw Error("render() method does not return ViewContext")
    }
}