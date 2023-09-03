package de.tschuehly.example.thymeleafkotlin.web.simple

import de.tschuehly.example.thymeleafkotlin.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.PostViewAction
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent

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