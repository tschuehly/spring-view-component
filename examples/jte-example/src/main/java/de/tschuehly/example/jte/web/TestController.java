package de.tschuehly.example.jte.web;

import de.tschuehly.example.jte.web.index.IndexViewComponent;
import de.tschuehly.example.jte.web.layout.LayoutViewComponent;
import de.tschuehly.example.jte.web.simple.SimpleViewComponent;
import de.tschuehly.spring.viewcomponent.jte.ViewContext;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    private final SimpleViewComponent simpleViewComponent;
    private final IndexViewComponent indexViewComponent;
    private final LayoutViewComponent layoutViewComponent;


    public TestController(
            SimpleViewComponent simpleViewComponent,
            IndexViewComponent indexViewComponent,
            LayoutViewComponent layoutViewComponent
            ) {
        this.simpleViewComponent = simpleViewComponent;
        this.indexViewComponent = indexViewComponent;
        this.layoutViewComponent = layoutViewComponent;
    }

    @GetMapping("/")
    ViewContext indexComponent() {
        return indexViewComponent.render();
    }

    @GetMapping("/simple")
    ViewContext simple() {
        return simpleViewComponent.render();
    }

    @GetMapping("/layout")
    ViewContext layoutComponent() {
        return layoutViewComponent.render(simpleViewComponent.render());
    }


    @GetMapping("/resource-template")
    String templateTest() {
        return "template-test";
    }
}
