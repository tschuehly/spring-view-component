package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.ViewProperty
import de.tschuehly.spring.viewcomponent.core.toMap
import gg.jte.Content
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput


class ViewContext(
    override vararg val contextAttributes: ViewProperty
) : Content, IViewContext {
    var jteTemplateEngine: TemplateEngine? = null
    override var componentBean: Any? = null
    override var componentTemplate: String? = null


    constructor(
        componentTemplate: String? = null,
        vararg contextAttributes: ViewProperty
    ) : this(*contextAttributes) {
        this.componentTemplate = componentTemplate

    }

    override fun writeTo(output: TemplateOutput) {

        jteTemplateEngine?.render("$componentTemplate.jte", contextAttributes.toMap(), output)
            ?: throw Error("JteTemplateEngine is null")
    }
}