package de.tschuehly.example.jte.web.simple;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.example.jte.core.ExampleService;
import de.tschuehly.spring.viewcomponent.jte.ViewContext;

@ViewComponent
public class SimpleViewComponent {
    final ExampleService exampleService;

    public SimpleViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public record SimpleView(String helloWorld) implements ViewContext {}

    public SimpleView render() {
        return new SimpleView(exampleService.getHelloWorld());
    }
}
