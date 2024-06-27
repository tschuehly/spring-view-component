package de.tschuehly.spring.viewcomponent.thymeleaf

import org.springframework.context.ApplicationContext
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor
import org.thymeleaf.standard.StandardDialect

class ThymeleafViewComponentDialect(private val applicationContext: ApplicationContext) : AbstractProcessorDialect(
    "ViewComponent Dialect", "view", StandardDialect.PROCESSOR_PRECEDENCE
) {
    override fun getProcessors(dialectPrefix: String): MutableSet<IProcessor> {
        return mutableSetOf(
            ThymeleafViewComponentTagProcessor(dialectPrefix,applicationContext)
        )

    }
}