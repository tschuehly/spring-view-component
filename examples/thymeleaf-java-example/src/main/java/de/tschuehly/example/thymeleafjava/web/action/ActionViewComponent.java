package de.tschuehly.example.thymeleafjava.web.action;

import de.tschuehly.example.thymeleafjava.core.ExampleService;
import de.tschuehly.spring.viewcomponent.core.action.*;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@ViewComponent
public class ActionViewComponent {
    private final ExampleService exampleService;

    public ActionViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public record ActionView(Integer counter, Map<Integer, String> itemList, Person person) implements ViewContext {
    }

    Integer counter = 0;

    public ViewContext render() {
        return new ActionView(counter, exampleService.itemList, person);
    }

    @GetViewAction(path = "/customPath/countUp")
    public ViewContext countUp() {
        counter += 1;
        return render();
    }


    record ActionFormDTO(
            String item
    ) {
    }

    @PostViewAction
    public ViewContext addItem(HttpServletRequest request,ActionFormDTO actionFormDTO) {
        exampleService.addItemToList(actionFormDTO.item);
        return render();
    }


    @DeleteViewAction
    public ViewContext deleteItem(Integer id) {
        exampleService.deleteItem(id);
        return render();
    }


    Person person = new Person(
            "Thomas", 23, "Ludwigsburg"
    );

    @PatchViewAction
    public ViewContext savePersonPatch(Person person) {
        this.person = person;
        return render();
    }

    @PutViewAction
    public ViewContext savePersonPut(HttpServletRequest request, Person person) {
        this.person = person; // TODO: ParameterBinding doesn't work
        return render();
    }

    public record Person(
            String name,
            Integer age,
            String location
    ) {
    }


}
