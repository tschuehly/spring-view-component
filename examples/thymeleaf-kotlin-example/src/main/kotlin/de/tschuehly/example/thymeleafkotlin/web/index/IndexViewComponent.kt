package de.tschuehly.example.thymeleafkotlin.web.index

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext

@ViewComponent
class IndexViewComponent {
    fun render() = IndexView()

    class IndexView : ViewContext
}