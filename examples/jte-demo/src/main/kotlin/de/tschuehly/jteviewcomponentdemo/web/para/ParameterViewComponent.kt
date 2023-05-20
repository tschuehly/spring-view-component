package de.tschuehly.jteviewcomponentdemo.web.para

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class ParameterViewComponent {
    fun render(parameter1: String) = ViewContext(
        "parameter1" toProperty parameter1
    )
}