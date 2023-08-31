package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.ViewProperty
import de.tschuehly.spring.viewcomponent.core.toMap
import gg.jte.Content
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput


class ViewContext(
) : Content, IViewContext {
    var templateSuffix: String? = null
    var jteTemplateEngine: TemplateEngine? = null


    override fun writeTo(output: TemplateOutput) {
        assert(templateSuffix != null)
        assert(jteTemplateEngine != null)
        IViewContext.componentTemplate
//        jteTemplateEngine!!.render("$componentTemplate$templateSuffix", contextAttributes.toMap(), output)
    }
}