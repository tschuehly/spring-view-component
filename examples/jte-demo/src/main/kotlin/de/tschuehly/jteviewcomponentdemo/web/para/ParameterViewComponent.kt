package de.tschuehly.jteviewcomponentdemo.web.para

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.JteViewContext

@ViewComponent
class ParameterViewComponent {
    fun render(parameter1: String) = JteViewContext(
        "parameter1" toProperty parameter1
    )
}