package de.tschuehly.jteviewcomponentdemo.web.index

import de.tschuehly.jteviewcomponentdemo.web.example.ExampleViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.JteViewContext

@ViewComponent
class IndexViewComponent(
    private val exampleViewComponent: ExampleViewComponent
) {

    fun render() = JteViewContext(
        "exampleFormDTO" toProperty ExampleFormDTO("Spring ViewComponent"),
        "exampleViewComponent" toProperty exampleViewComponent
    )

    class ExampleFormDTO(
        val exampleProp: String
    ) {
    }
}