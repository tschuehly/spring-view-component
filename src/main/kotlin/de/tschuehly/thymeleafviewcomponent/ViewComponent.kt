package de.tschuehly.thymeleafviewcomponent

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ViewComponent()

@Aspect
@Component
class ViewComponentAspect(
    val applicationContext: ApplicationContext
) {
    @Around("execution(* render()) || execution(* render(*))")
    fun renderInject(joinPoint: ProceedingJoinPoint): ViewComponentContext {
        val viewComponentContext = joinPoint.proceed() as ViewComponentContext
        return ViewComponentContext(viewComponentContext.context,joinPoint.`this`.javaClass)
    }
}