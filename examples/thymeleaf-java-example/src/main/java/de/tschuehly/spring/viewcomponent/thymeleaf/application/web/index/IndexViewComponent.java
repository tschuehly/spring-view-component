package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.index;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.EmptyViewContext;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;

@ViewComponent
public class IndexViewComponent {
    public ViewContext render(){
        return new EmptyViewContext();
    }
}
