package de.tschuehly.kteviewcomponentexample.web.layout

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.kte.ViewContext

@ViewComponent
class LayoutViewComponent {
    data class LayoutView(val nestedViewComponent: ViewContext) : ViewContext

    fun render(nestedViewComponent: ViewContext) = LayoutView(nestedViewComponent)

}