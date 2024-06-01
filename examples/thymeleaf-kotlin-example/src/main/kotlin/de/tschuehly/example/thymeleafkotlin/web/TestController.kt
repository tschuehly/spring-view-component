package de.tschuehly.example.thymeleafkotlin.web

import de.tschuehly.example.thymeleafkotlin.web.index.IndexViewComponent
import de.tschuehly.example.thymeleafkotlin.web.layout.LayoutViewComponent
import de.tschuehly.example.thymeleafkotlin.web.simple.SimpleViewComponent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TestController(
    private val simpleViewComponent: SimpleViewComponent,
    private val indexViewComponent: IndexViewComponent,
    private val layoutViewComponent: LayoutViewComponent
) {

    @GetMapping("/")
    fun indexComponent() = indexViewComponent.render()

    @GetMapping("/simple")
    fun simpleComponent() = simpleViewComponent.render()

    @GetMapping("/layout")
    fun layoutComponent() = layoutViewComponent.render(simpleViewComponent.render())


    @GetMapping("/resource-template")
    fun templateTest(): String {
        return "template-test"
    }
}