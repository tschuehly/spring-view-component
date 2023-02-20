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
    fun renderInject(joinPoint: ProceedingJoinPoint): Any {
        val contextMap: Map<String, Any> = joinPoint.proceed() as Map<String, Any>
        val htmlContext = Context()
        htmlContext.setVariables(contextMap)
        htmlContext.setVariable(
            ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
            ThymeleafEvaluationContext(applicationContext, null)
        )
        val engine = TemplateEngineConfig.templateEngine(joinPoint.`this`.javaClass)

        return engine
            .process(
                joinPoint.`this`.javaClass.simpleName.substringBefore("$$"),
                htmlContext
            )
    }
}