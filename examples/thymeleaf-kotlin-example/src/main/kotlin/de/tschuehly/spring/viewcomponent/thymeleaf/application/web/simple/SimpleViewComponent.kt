package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.simple

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.PostViewAction
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.thymeleaf.application.core.ExampleService

@ViewComponent
class SimpleViewComponent(
    val exampleService: ExampleService
) {
    fun render() = HelloWorldView(exampleService.getHelloWorld())

    data class HelloWorldView(val helloWorld: String) : IViewContext

    @PostViewAction
    fun testAction(): IViewContext {
        return render()
    }
}