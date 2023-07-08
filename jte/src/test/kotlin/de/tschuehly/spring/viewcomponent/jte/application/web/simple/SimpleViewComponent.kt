package de.tschuehly.spring.viewcomponent.jte.application.web.simple

import de.tschuehly.spring.viewcomponent.core.PostViewAction
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext
import de.tschuehly.spring.viewcomponent.jte.application.core.ExampleService

@ViewComponent
class SimpleViewComponent(
    val exampleService: ExampleService
) {
    fun render() = ViewContext(
        "helloWorld" toProperty exampleService.getHelloWorld()
    )

    @PostViewAction
    fun testAction(): ViewContext {
        return render()
    }
}