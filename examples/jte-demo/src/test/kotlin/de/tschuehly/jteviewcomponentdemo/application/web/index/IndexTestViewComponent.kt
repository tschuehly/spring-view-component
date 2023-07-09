package de.tschuehly.jteviewcomponentdemo.application.web.index

import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class IndexTestViewComponent(
) {

    fun render() = ViewContext(
        "test" toProperty "Hello World"
    )

}