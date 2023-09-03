package de.tschuehly.example.jte.web.simple;

import de.tschuehly.spring.viewcomponent.core.IViewContext;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.example.jte.core.ExampleService;

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