package de.tschuehly.thymeleafviewcomponent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;

import java.util.HashMap;

@Aspect
@Component
public class ViewComponentAspect {
    private ApplicationContext applicationContext;
    public ViewComponentAspect(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Around("execution(* render()) || execution(* render(*))")
    Object renderViewComponent(ProceedingJoinPoint joinPoint) throws Throwable {

        HashMap<String, Object> contextMap =  getJoinPointCast(joinPoint.proceed());
        Context htmlContext = new Context();
        htmlContext.setVariables(contextMap);
        htmlContext.setVariable(
                ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                new ThymeleafEvaluationContext(applicationContext, null)
        );
        SpringTemplateEngine engine = ViewComponentTemplateEngineConfig.templateEngine(joinPoint.getThis().getClass());
        String simpleName = joinPoint.getThis().getClass().getSimpleName();
        return engine.process(
                StringUtils.split(simpleName,"$$")[0],
                htmlContext
        );

    }
    private <T> HashMap<String, Object> getJoinPointCast(Object joinPointReturn){
        return ((HashMap<String,Object>) joinPointReturn);
    }

}
