package de.tschuehly.thymeleafviewcomponent

import org.springframework.format.Formatter
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.util.*

class ViewComponentFormatter(
    val springTemplateEngine: SpringTemplateEngine
) : Formatter<ViewComponentContext> {
    override fun print(viewComponentContext: ViewComponentContext, locale: Locale): String {
        if(viewComponentContext.componentClass == null){
            throw Error("ViewComponentContext componentclass is null")
        }
        val context = Context()
        context.setVariables(viewComponentContext.context)
        val componentName =
            viewComponentContext.componentClass.simpleName.substringBefore("$$")
        val componentPackage =
            viewComponentContext.componentClass.`package`.name.replace(".","/") + "/"

        return springTemplateEngine.process("$componentPackage$componentName.html",context)
    }

    override fun parse(text: String, locale: Locale): ViewComponentContext {
        TODO("Not yet implemented")
    }

}