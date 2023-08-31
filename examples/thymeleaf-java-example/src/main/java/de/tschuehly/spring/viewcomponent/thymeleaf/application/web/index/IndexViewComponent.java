package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.index;

import de.tschuehly.spring.viewcomponent.core.EmptyViewContext;
import de.tschuehly.spring.viewcomponent.core.IViewContext;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;

@ViewComponent
public class IndexViewComponent {
    public IViewContext render() {
        return new EmptyViewContext();
    }
}

