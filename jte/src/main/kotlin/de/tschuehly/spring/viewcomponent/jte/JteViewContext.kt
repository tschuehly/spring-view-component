package de.tschuehly.spring.viewcomponent.jte

import de.tschuehly.spring.viewcomponent.core.ViewContext
import de.tschuehly.spring.viewcomponent.core.ViewProperty
import de.tschuehly.spring.viewcomponent.core.toMap
import gg.jte.Content
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput


class JteViewContext(
    override vararg val contextAttributes: ViewProperty,
) : Content, ViewContext(contextAttributes = contextAttributes) {
    var jteTemplateEngine: TemplateEngine? = null

    override fun writeTo(output: TemplateOutput) {

        jteTemplateEngine?.render(componentTemplate + ".jte", contextAttributes.toMap(), output)
            ?: throw Error("JteTemplateEngine is null")
    }
}