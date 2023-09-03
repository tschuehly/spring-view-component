package de.tschuehly.example.thymeleafjava.web;

import de.tschuehly.example.thymeleafjava.web.layout.LayoutViewComponent;
import de.tschuehly.example.thymeleafjava.web.index.IndexViewComponent;
import de.tschuehly.example.thymeleafjava.web.action.ActionViewComponent;
import de.tschuehly.example.thymeleafjava.web.simple.SimpleViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    private final SimpleViewComponent simpleViewComponent;
    private final IndexViewComponent indexViewComponent;
    private final ActionViewComponent actionViewComponent;
    private final LayoutViewComponent layoutViewComponent;


    public TestController(
            SimpleViewComponent simpleViewComponent,
            IndexViewComponent indexViewComponent,
            ActionViewComponent actionViewComponent,
            LayoutViewComponent layoutViewComponent
            ) {
        this.simpleViewComponent = simpleViewComponent;
        this.indexViewComponent = indexViewComponent;
        this.actionViewComponent = actionViewComponent;
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

    @GetMapping("/action")
    ViewContext actionComponent() {
        return actionViewComponent.render();
    }

    @GetMapping("/nested-action")
    ViewContext nestedActionComponent() {
        return layoutViewComponent.render(actionViewComponent.render());
    }


    @GetMapping("/resource-template")
    String templateTest() {
        return "template-test";
    }
}
