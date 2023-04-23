package de.tschuehly.jteviewcomponentdemo.web.example

import de.tschuehly.jteviewcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.JteViewContext

@ViewComponent
class ExampleViewComponent(
    private val exampleService: ExampleService
) {
    fun render() = JteViewContext(
        "exampleService" toProperty exampleService
    )
}