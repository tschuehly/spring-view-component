package de.tschuehly.thymeleafviewcomponent

import org.springframework.format.Formatter
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.util.*

class ViewComponentFormatter(
    private val springTemplateEngine: SpringTemplateEngine
) : Formatter<ViewContext> {
    override fun print(viewContext: ViewContext, locale: Locale): String {
        if(viewContext.componentTemplate == null){
            throw Error("ViewContext componentTemplate is null")
        }
        val context = Context()
        context.setVariables(viewContext.contextAttributes.toMap())

        return springTemplateEngine.process(viewContext.componentTemplate,context)
    }

    override fun parse(text: String, locale: Locale): ViewContext {
        throw Error("Parsing of ViewContext is not yet supported")
    }

}