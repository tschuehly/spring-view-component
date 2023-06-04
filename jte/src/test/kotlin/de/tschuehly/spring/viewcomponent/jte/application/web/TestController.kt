package de.tschuehly.spring.viewcomponent.jte.application.web

import de.tschuehly.spring.viewcomponent.jte.application.web.simple.SimpleViewComponent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TestController(
    private val simpleViewComponent: SimpleViewComponent
) {

    @GetMapping("/simpleComponent")
    fun simpleComponent() = simpleViewComponent.render()
}