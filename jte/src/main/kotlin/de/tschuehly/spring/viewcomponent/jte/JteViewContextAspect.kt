package de.tschuehly.spring.viewcomponent.jte

import gg.jte.TemplateEngine
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class JteViewContextAspect(
    private val jteTemplateEngine: TemplateEngine,
    private val jteProperties: JteProperties
) {
    @Pointcut("@within(de.tschuehly.spring.viewcomponent.core.component.ViewComponent)")
    fun isViewComponent() {
        //
    }

    @Pointcut("execution(* render(..)) || execution(* get(..))")
    fun isRenderOrGetMethod() {
        //
    }

    @AfterReturning(pointcut = "isViewComponent() && isRenderOrGetMethod()", returning = "viewContext")
    fun renderInject(viewContext: ViewContext): ViewContext {
        viewContext.jteTemplateEngine = jteTemplateEngine
        viewContext.templateSuffix = jteProperties.templateSuffix
        return viewContext
    }
}