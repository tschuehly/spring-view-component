package de.tschuehly.example.thymeleafjava.web.simple;

import de.tschuehly.example.thymeleafjava.core.ExampleService;
import de.tschuehly.spring.viewcomponent.core.IViewContext;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;

@ViewComponent
public class SimpleViewComponent {
    final ExampleService exampleService;

    public SimpleViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    record SimpleView(String helloWorld)implements IViewContext{}

    public SimpleView render() {
        return new SimpleView(exampleService.getHelloWorld());
    }
}
