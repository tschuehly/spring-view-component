package com.example.thymeleafcomponentdemo.web.parameter

import com.example.thymeleafcomponentdemo.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContext
import de.tschuehly.spring.viewcomponent.core.toProperty


@ViewComponent
class ParameterViewComponent(
    private val exampleService: ExampleService
) {
    fun render(parameter: String?, parameter2: String?) = ViewContext(
        "testparameter" toProperty (parameter ?: throw Exception("You need to pass in a parameter")),
        "office" toProperty (parameter2 ?: exampleService.getOfficeHours())
    )
}