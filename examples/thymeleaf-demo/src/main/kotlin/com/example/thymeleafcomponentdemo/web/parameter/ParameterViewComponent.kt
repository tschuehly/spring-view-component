package com.example.thymeleafcomponentdemo.web.parameter

import com.example.thymeleafcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext


@ViewComponent
class ParameterViewComponent(
    private val exampleService: ExampleService
) {
    fun render(parameter: String?, parameter2: String?) = ViewContext(
        "testparameter" toProperty (parameter ?: throw Exception("You need to pass in a parameter")),
        "office" toProperty (parameter2 ?: exampleService.getOfficeHours())
    )
}