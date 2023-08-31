package de.tschuehly.jteviewcomponentdemo.web

import de.tschuehly.jteviewcomponentdemo.web.action.ActionViewComponent
import de.tschuehly.jteviewcomponentdemo.web.index.IndexViewComponent
import de.tschuehly.jteviewcomponentdemo.web.layout.LayoutViewComponent
import de.tschuehly.jteviewcomponentdemo.web.simple.SimpleViewComponent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute

@Controller
class TestController(
    private val simpleViewComponent: SimpleViewComponent,
    private val indexViewComponent: IndexViewComponent,
    private val layoutViewComponent: LayoutViewComponent,
    private val actionViewComponent: ActionViewComponent
) {

    @GetMapping("/")
    fun indexComponent() = indexViewComponent.render()

    @GetMapping("/simple")
    fun simpleComponent() = simpleViewComponent.render()

    @GetMapping("/layout")
    fun layoutComponent() = layoutViewComponent.render(simpleViewComponent.render())

    @GetMapping("/action")
    fun actionComponent() = actionViewComponent.render()

    @GetMapping("/nested-action")
    fun nestedActionComponent() = layoutViewComponent.render(actionViewComponent.render())


    @GetMapping("/resource-template")
    fun templateTest(): String {
        return "template-test"
    }
}