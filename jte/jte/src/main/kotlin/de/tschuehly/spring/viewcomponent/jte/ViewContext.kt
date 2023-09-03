package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.getAttributes
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.getComponentName
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.getTemplate
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.jteTemplateEngine
import gg.jte.Content
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput


interface ViewContext : Content, IViewContext {
    override fun writeTo(output: TemplateOutput) {
        output.writeContent("<div id=\"")
        output.writeContent(getComponentName(this))
        output.writeContent("\" style=\"display: contents;\">")
        (jteTemplateEngine as TemplateEngine).render(
            getTemplate(this), getAttributes(this),
            output
        )
        output.writeContent("</div>")
    }
}

class EmptyViewContext() : ViewContext
