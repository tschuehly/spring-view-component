package de.tschuehly.spring.viewcomponent.jte.application.web.layout

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class LayoutViewComponent {
    fun render(nestedViewComponent: ViewContext) = ViewContext(
        "nestedViewComponent" toProperty nestedViewComponent
    )
}