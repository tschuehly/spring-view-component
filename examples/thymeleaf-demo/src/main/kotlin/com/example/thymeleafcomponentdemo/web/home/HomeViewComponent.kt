package com.example.thymeleafcomponentdemo.web.home

import com.example.thymeleafcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContext
import de.tschuehly.spring.viewcomponent.core.toProperty

@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService,
) {
    fun render() = ViewContext(
            "helloWorld" toProperty exampleService.getHelloWorld(),
            "coffee" toProperty exampleService.getCoffee()
    )
}
