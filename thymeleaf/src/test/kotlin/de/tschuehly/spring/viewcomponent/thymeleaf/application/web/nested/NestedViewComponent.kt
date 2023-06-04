package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.nested

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import de.tschuehly.spring.viewcomponent.thymeleaf.application.web.simple.SimpleViewComponent

@ViewComponent
class NestedViewComponent(
    private val simpleViewComponent: SimpleViewComponent
) {
    fun render() = ViewContext(
        "simpleViewComponent" toProperty simpleViewComponent.render()
    )
}