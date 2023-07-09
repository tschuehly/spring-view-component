package de.tschuehly.spring.viewcomponent.jte.application.web.header


import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class HeaderViewComponent {
    fun render(title: String) = ViewContext(
        "title" toProperty title
    )
}