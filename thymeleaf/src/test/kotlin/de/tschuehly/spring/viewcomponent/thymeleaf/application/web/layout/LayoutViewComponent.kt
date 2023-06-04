package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.layout

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext

@ViewComponent
class LayoutViewComponent {
    fun render(nestedViewComponent: ViewContext) = ViewContext(
        "nestedViewComponent" toProperty nestedViewComponent
    )
}