package de.tschuehly.kteviewcomponentexample.web.index

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class IndexViewComponent {
    fun render() = IndexView()

    class IndexView : ViewContext
}