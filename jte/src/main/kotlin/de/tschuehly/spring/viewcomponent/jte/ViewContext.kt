package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.exception.RenderException
import gg.jte.Content
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput


interface ViewContext : Content, IViewContext {
    companion object{
        var templateSuffix: String? = null
        var templateEngine: TemplateEngine? = null
    }
    override fun writeTo(output: TemplateOutput) {
        templateEngine?.render(
            IViewContext.getViewComponentTemplateWithoutSuffix(this) + templateSuffix,
            this,
            output
        ) ?: throw RenderException("Template Engine not initialized")
    }
}