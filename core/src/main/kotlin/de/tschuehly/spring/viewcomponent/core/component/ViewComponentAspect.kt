package de.tschuehly.spring.viewcomponent.core.component

import de.tschuehly.spring.viewcomponent.core.IViewContext
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class ViewComponentAspect {
    @Pointcut("@within(de.tschuehly.spring.viewcomponent.core.component.ViewComponent)")
    fun isViewComponent() {
        //
    }

    @Around("isViewComponent() && execution(public de.tschuehly.spring.viewcomponent.core.IViewContext+ *(..)) ")
    fun renderInject(joinPoint: ProceedingJoinPoint): IViewContext {
        val returnValue = joinPoint.proceed()
        val viewContext = if (IViewContext::class.java.isAssignableFrom(returnValue.javaClass)) {
            returnValue as IViewContext
        } else {
            throw ViewComponentException("${returnValue.javaClass} needs to implement ViewContext abstract class")
        }
        IViewContext.componentBean = joinPoint.target
        IViewContext.componentTemplate = IViewContext.getViewComponentTemplateWithoutSuffix(viewContext)
        return viewContext
    }
}