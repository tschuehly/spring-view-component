package de.tschuehly.thymeleafviewcomponent

import org.springframework.format.Formatter
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.util.*

class ViewComponentFormatter(
    private val springTemplateEngine: SpringTemplateEngine
) : Formatter<ViewComponentContext> {
    override fun print(viewComponentContext: ViewComponentContext, locale: Locale): String {
        if(viewComponentContext.componentTemplate == null){
            throw Error("ViewComponentContext componentTemplate is null")
        }
        val context = Context()
        context.setVariables(viewComponentContext.contextAttributes.toMap())

        return springTemplateEngine.process(viewComponentContext.componentTemplate,context)
    }

    override fun parse(text: String, locale: Locale): ViewComponentContext {
        throw Error("Parsing of ViewComponentContext is not yet supported")
    }

}