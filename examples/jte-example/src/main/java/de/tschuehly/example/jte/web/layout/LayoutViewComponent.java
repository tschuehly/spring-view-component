package de.tschuehly.example.jte.web.layout;

import de.tschuehly.spring.viewcomponent.core.IViewContext;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;

@ViewComponent
public
class LayoutViewComponent {

    private record LayoutView(IViewContext nestedViewComponent) implements IViewContext{}

    public IViewContext render(IViewContext nestedViewComponent) {
        return new LayoutView(nestedViewComponent);
    }
}