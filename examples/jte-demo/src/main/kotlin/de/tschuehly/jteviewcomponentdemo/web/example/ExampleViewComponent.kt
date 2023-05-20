package de.tschuehly.jteviewcomponentdemo.web.example

import de.tschuehly.jteviewcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class ExampleViewComponent(
    private val exampleService: ExampleService
) {
    fun render() = ViewContext(
        "exampleService" toProperty exampleService
    )
}