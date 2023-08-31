package de.tschuehly.jteviewcomponentdemo.web.simple

import de.tschuehly.jteviewcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.PostViewAction
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class SimpleViewComponent(
    val exampleService: ExampleService
) {
    fun render(): IViewContext {
        return HelloWorldView(exampleService.getHelloWorld())
    }

    data class HelloWorldView(val helloWorld: String) : IViewContext

    @PostViewAction
    fun testAction(): IViewContext {
        return render()
    }
}