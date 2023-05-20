package de.tschuehly.spring.viewcomponent.jte

import gg.jte.TemplateEngine
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class JteViewContextAspect(
    private val jteTemplateEngine: TemplateEngine
) {
    @Pointcut("@within(de.tschuehly.spring.viewcomponent.core.ViewComponent)")
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
        return viewContext
    }
}