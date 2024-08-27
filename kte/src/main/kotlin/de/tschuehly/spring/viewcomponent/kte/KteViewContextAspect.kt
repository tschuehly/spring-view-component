package de.tschuehly.spring.viewcomponent.kte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import gg.jte.TemplateEngine
import gg.jte.springframework.boot.autoconfigure.JteProperties
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

@Aspect
class KteViewContextAspect(
    private val jteTemplateEngine: TemplateEngine,
    private val jteProperties: JteProperties
) {
    @Pointcut("@within(de.tschuehly.spring.viewcomponent.core.component.ViewComponent)")
    fun isViewComponent() {
        //
    }

    @Pointcut("execution(de.tschuehly.spring.viewcomponent.kte.ViewContext+ *(..))")
    fun isRenderOrGetMethod() {
        //
    }

    @AfterReturning(pointcut = "isViewComponent() && isRenderOrGetMethod()", returning = "viewContext")
    fun renderInject(viewContext: ViewContext): ViewContext {
        ViewContext.templateEngine = jteTemplateEngine
        ViewContext.templateSuffix = jteProperties.templateSuffix
        return viewContext
    }
}