package de.tschuehly.example.thymeleafkotlin.web.simple

import de.tschuehly.example.thymeleafkotlin.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext

@ViewComponent
class SimpleViewComponent(
    val exampleService: ExampleService
) {
    fun render() = SimpleView(exampleService.getHelloWorld())

    data class SimpleView(val helloWorld: String) : ViewContext

}