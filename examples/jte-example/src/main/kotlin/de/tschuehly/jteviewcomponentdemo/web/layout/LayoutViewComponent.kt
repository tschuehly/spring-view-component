package de.tschuehly.jteviewcomponentdemo.web.layout

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.jte.ViewContext

@ViewComponent
class LayoutViewComponent {
    data class LayoutView(val nestedViewComponent: ViewContext) : ViewContext

    fun render(nestedViewComponent: ViewContext) = LayoutView(nestedViewComponent)

}