package de.tschuehly.spring.viewcomponent.jte.application

import de.tschuehly.spring.viewcomponent.jte.application.web.index.IndexViewComponent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ComponentController(
    private val indexViewComponent: IndexViewComponent
) {
    @GetMapping("/index")
    fun index() = indexViewComponent.render()
}