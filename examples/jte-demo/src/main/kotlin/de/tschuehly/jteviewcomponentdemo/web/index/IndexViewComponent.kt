package de.tschuehly.jteviewcomponentdemo.web.index

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContext
import de.tschuehly.spring.viewcomponent.core.toProperty

@ViewComponent
class IndexViewComponent(
) {

    fun render() = ViewContext(
        "model" toProperty ModelTest("Thomas")
    )

}