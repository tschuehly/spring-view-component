package de.tschuehly.example.jte.web.layout;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.jte.ViewContext;

@ViewComponent
public
class LayoutViewComponent {

    public record LayoutView(ViewContext nestedViewComponent) implements ViewContext {
    }

    public ViewContext render(ViewContext nestedViewComponent) {
        return new LayoutView(nestedViewComponent);
    }
}