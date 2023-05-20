package de.tschuehly.spring.viewcomponent.thymeleaf

import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor
import org.thymeleaf.standard.StandardDialect

class ThymeleafViewComponentDialect : AbstractProcessorDialect(
    "ViewComponent Dialect", "view", StandardDialect.PROCESSOR_PRECEDENCE
) {
    override fun getProcessors(dialectPrefix: String): MutableSet<IProcessor> {
        return mutableSetOf(
            ThymeleafViewComponentProcessor(dialectPrefix)
        )

    }
}