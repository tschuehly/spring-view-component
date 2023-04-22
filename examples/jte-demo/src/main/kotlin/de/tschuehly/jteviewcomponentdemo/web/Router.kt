package de.tschuehly.jteviewcomponentdemo.web

import de.tschuehly.jteviewcomponentdemo.web.index.IndexViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Router(
    private val indexViewComponent: IndexViewComponent
) {
    @GetMapping("/")
    fun index(): ViewContext {
        return indexViewComponent.render()
    }

}
