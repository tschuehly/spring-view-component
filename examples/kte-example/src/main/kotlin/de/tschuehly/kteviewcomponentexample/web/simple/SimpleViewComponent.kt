package de.tschuehly.kteviewcomponentexample.web.simple

import de.tschuehly.kteviewcomponentexample.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.kte.ViewContext

@ViewComponent
class SimpleViewComponent(
    val exampleService: ExampleService
) {
    fun render() = SimpleView(exampleService.getHelloWorld())

    data class SimpleView(val helloWorld: String) : ViewContext

}