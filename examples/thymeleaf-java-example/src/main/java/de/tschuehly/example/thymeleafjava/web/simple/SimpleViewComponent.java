package de.tschuehly.example.thymeleafjava.web.simple;

import de.tschuehly.example.thymeleafjava.core.ExampleService;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;

@ViewComponent
public class SimpleViewComponent {
    final ExampleService exampleService;

    public SimpleViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public record SimpleView(String helloWorld) implements ViewContext {
    }

    public SimpleView render() {
        return new SimpleView(exampleService.getHelloWorld());
    }
}
