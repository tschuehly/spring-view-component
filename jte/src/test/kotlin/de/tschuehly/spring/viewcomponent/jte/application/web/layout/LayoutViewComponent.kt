package de.tschuehly.spring.viewcomponent.jte.application.web.layout

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext
import de.tschuehly.spring.viewcomponent.jte.application.web.simple.SimpleViewComponent

@ViewComponent
class LayoutViewComponent(
) {
    fun render(nestedComponent: ViewContext) = ViewContext(
        "nestedComponent" toProperty nestedComponent
    )

}