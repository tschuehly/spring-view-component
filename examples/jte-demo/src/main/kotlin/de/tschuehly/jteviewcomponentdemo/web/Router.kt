package de.tschuehly.jteviewcomponentdemo.web

import de.tschuehly.jteviewcomponentdemo.web.index.IndexViewComponent
import de.tschuehly.spring.viewcomponent.jte.JteViewContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Router(
    private val indexViewComponent: IndexViewComponent
) {
    @GetMapping("/")
    fun index(): JteViewContext {
        return indexViewComponent.render()
    }

}
