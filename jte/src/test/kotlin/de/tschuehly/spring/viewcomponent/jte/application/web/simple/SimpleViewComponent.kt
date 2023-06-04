package de.tschuehly.spring.viewcomponent.jte.application.web.simple

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class SimpleViewComponent {
    fun render() = ViewContext(
        "helloWorld" toProperty "Hello World"
    )
}