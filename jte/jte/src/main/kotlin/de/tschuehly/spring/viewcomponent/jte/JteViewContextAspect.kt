package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentProperties
import gg.jte.TemplateEngine
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class JteViewContextAspect(
    private val jteTemplateEngine: TemplateEngine, private val viewComponentProperties: ViewComponentProperties
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
    fun renderInject(viewContext: IViewContext): IViewContext {
        IViewContext.jteTemplateEngine = jteTemplateEngine
        IViewContext.templateSuffx = viewComponentProperties.jteTemplateSuffix
        return viewContext
    }
}