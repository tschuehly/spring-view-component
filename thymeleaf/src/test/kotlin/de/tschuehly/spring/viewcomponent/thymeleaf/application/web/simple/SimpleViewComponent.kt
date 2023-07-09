package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.simple

import de.tschuehly.spring.viewcomponent.core.action.PostViewAction
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import de.tschuehly.spring.viewcomponent.thymeleaf.application.core.ExampleService

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