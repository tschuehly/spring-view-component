package de.tschuehly.jteviewcomponentdemo.web.index

import de.tschuehly.jteviewcomponentdemo.web.example.ExampleViewComponent
import de.tschuehly.jteviewcomponentdemo.web.para.ParameterViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class IndexViewComponent(
    private val exampleViewComponent: ExampleViewComponent,
    private val parameterViewComponent: ParameterViewComponent
) {

    fun render() = ViewContext(
        "exampleFormDTO" toProperty ExampleFormDTO("Spring ViewComponent"),
        "exampleViewComponent" toProperty exampleViewComponent,
        "parameterViewComponent" toProperty parameterViewComponent.render("Hello World")
    )

    class ExampleFormDTO(
        val exampleProp: String
    ) {
    }
}