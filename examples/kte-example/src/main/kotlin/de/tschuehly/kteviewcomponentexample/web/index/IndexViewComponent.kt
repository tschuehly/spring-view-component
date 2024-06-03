package de.tschuehly.kteviewcomponentexample.web.index

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.kte.ViewContext

@ViewComponent
class IndexViewComponent {
    fun render() = IndexView()

    class IndexView : ViewContext
}