package de.tschuehly.jteviewcomponentdemo.application.web

import de.tschuehly.jteviewcomponentdemo.application.web.index.IndexTestViewComponent
import de.tschuehly.spring.viewcomponent.jte.ViewContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Router(
    private val indexTestViewComponent: IndexTestViewComponent
) {
    @GetMapping("/")
    fun index(): ViewContext {
        return indexTestViewComponent.render()
    }

}
