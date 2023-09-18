package de.tschuehly.example.jte.web.index;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.jte.ViewContext;

@ViewComponent
public class IndexViewComponent {
    public ViewContext render() {
        return new IndexView();
    }

    record IndexView() implements ViewContext {
    }
}
