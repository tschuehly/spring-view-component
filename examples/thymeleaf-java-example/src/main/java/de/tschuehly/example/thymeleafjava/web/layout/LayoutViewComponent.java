package de.tschuehly.example.thymeleafjava.web.layout;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;

@ViewComponent
public
class LayoutViewComponent {

    private record LayoutView(ViewContext nestedViewComponent) implements ViewContext {
    }

    public ViewContext render(ViewContext nestedViewComponent) {
        return new LayoutView(nestedViewComponent);
    }
}