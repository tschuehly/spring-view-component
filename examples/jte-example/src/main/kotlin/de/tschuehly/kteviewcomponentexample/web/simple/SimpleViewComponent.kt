package de.tschuehly.kteviewcomponentexample.web.simple

import de.tschuehly.kteviewcomponentexample.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.PostViewAction
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class SimpleViewComponent(
    val exampleService: ExampleService
) {
    fun render() = HelloWorldView(exampleService.getHelloWorld())

    data class HelloWorldView(val helloWorld: String) : ViewContext

    @PostViewAction
    fun testAction(): IViewContext {
        return render()
    }
}