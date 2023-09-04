package de.tschuehly.example.jte.core;

import de.tschuehly.spring.viewcomponent.core.ViewProperty;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;
import io.alanda.davinci.frontend.htmx.components.table.models.RowClick;
import io.alanda.davinci.frontend.htmx.components.table.models.TableLayout;

import java.util.List;

@ViewComponent
public class TableComponent {

  public ViewContext render(TableLayout layout, List<?> items) {
    return render(layout, items, null);
  }

  public ViewContext render(TableLayout layout, List<?> items , RowClick rowClick) {
    var hasParameter = rowClick != null && rowClick.parameterColumnIdentifierField() != null;
    return new ViewContext(
        List.of(
            new ViewProperty("layout", layout),
            new ViewProperty("items", items),
            new ViewProperty("rowClick", rowClick),
            new ViewProperty("hasParameter", hasParameter)
        ).toArray(new ViewProperty[0]));
  }

}
