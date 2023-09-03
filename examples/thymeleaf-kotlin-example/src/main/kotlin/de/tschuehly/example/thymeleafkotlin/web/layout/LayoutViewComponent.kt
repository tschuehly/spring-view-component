package de.tschuehly.example.thymeleafkotlin.web.layout

import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent

@ViewComponent
class LayoutViewComponent {
    data class LayoutView(val nestedViewComponent: IViewContext) : IViewContext

    fun render(nestedViewComponent: IViewContext) = LayoutView(nestedViewComponent)

}