package de.tschuehly.spring.viewcomponent.jte.application.web

import de.tschuehly.spring.viewcomponent.jte.application.web.layout.LayoutViewComponent
import de.tschuehly.spring.viewcomponent.jte.application.web.simple.SimpleViewComponent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TestController(
    private val simpleViewComponent: SimpleViewComponent,
    private val layoutViewComponent: LayoutViewComponent
) {

    @GetMapping("/layout")
    fun layoutComponent() = layoutViewComponent.render(
        simpleViewComponent.render()
    )
    @GetMapping("/simpleComponent")
    fun simpleComponent() = simpleViewComponent.render()
}