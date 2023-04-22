package com.example.thymeleafcomponentdemo.web.footer

import com.example.thymeleafcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContext
import de.tschuehly.spring.viewcomponent.core.toProperty

@ViewComponent
class FooterViewComponent (
    private val exampleService: ExampleService,
    ) {
        fun render() = ViewContext(
            "helloWorld" toProperty  exampleService.getHelloWorld()
        )
}