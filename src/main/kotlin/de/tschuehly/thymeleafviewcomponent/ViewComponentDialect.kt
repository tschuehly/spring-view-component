package de.tschuehly.thymeleafviewcomponent

import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor
import org.thymeleaf.standard.StandardDialect

class ViewComponentDialect : AbstractProcessorDialect(
    "ViewComponent Dialect", "view", StandardDialect.PROCESSOR_PRECEDENCE
) {
    override fun getProcessors(dialectPrefix: String): MutableSet<IProcessor> {
        return mutableSetOf(
            ViewComponentProcessor(dialectPrefix)
        )

    }
}