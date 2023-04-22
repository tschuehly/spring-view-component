package com.example.thymeleafcomponentdemo.web.navigation


import com.example.thymeleafcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContext
import de.tschuehly.spring.viewcomponent.core.toProperty

@ViewComponent
class NavigationViewComponent(
    private val exampleService: ExampleService
) {
    var counter = 0
    fun render(): ViewContext {
        counter = counter.plus(1)
        return ViewContext(
            "someOtherProperty" toProperty exampleService.getSomeOtherProperty(),
            "counter" toProperty counter
        )
    }
}