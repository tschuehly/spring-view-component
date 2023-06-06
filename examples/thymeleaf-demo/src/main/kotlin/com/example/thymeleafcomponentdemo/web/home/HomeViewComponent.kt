package com.example.thymeleafcomponentdemo.web.home

import com.example.thymeleafcomponentdemo.core.ExampleService
import com.example.thymeleafcomponentdemo.web.table.TableViewComponent
import com.example.thymeleafcomponentdemo.web.parameter.ParameterViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext

@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService,
    private val tableViewComponent: TableViewComponent,
    private val parameterViewComponent: ParameterViewComponent,
) {
    fun render() = ViewContext(
        "helloWorld" toProperty exampleService.getHelloWorld(),
        "tableViewComponent" toProperty tableViewComponent.render(),
        "parameterViewComponent" toProperty parameterViewComponent.render(exampleService.getCoffee(), null)
    )
}
