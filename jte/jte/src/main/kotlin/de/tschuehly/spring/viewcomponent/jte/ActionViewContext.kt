package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentUtils
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput

interface ActionViewContext: ViewContext {
    override fun writeTo(output: TemplateOutput) {
        output.writeContent("<div id=\"")
        output.writeContent(ViewComponentUtils.getId(this::class.java))
        output.writeContent("\" style=\"display: contents;\">")
        (IViewContext.jteTemplateEngine as TemplateEngine).render(
            IViewContext.getViewComponentTemplate(this), this,
            output
        )
        output.writeContent("</div>")
    }
}