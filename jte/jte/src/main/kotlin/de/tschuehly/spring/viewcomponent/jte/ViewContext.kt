package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.getAttributes
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.getViewComponentName
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.getViewComponentTemplate
import de.tschuehly.spring.viewcomponent.core.IViewContext.Companion.jteTemplateEngine
import de.tschuehly.spring.viewcomponent.core.component.ViewComponentUtils
import gg.jte.Content
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput


interface ViewContext : Content, IViewContext {
    override fun writeTo(output: TemplateOutput) {
        (jteTemplateEngine as TemplateEngine).render(
            getViewComponentTemplate(this), this,
            output
        )
    }
}