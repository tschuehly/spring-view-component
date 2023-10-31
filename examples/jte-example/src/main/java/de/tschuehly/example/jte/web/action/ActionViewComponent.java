package de.tschuehly.example.jte.web.action;

import de.tschuehly.example.jte.core.ExampleService;
import de.tschuehly.spring.viewcomponent.core.IViewContext;
import de.tschuehly.spring.viewcomponent.core.action.*;
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.jte.ActionViewContext;
import de.tschuehly.spring.viewcomponent.jte.ViewContext;

import java.util.Map;

@ViewComponent
public class ActionViewComponent {
    private final ExampleService exampleService;

    public ActionViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public record ActionView(Integer counter, Map<Integer, String> itemList, Person person) implements
        ActionViewContext {    }

    Integer counter = 0;

    public ActionView render() {
        return new ActionView(counter, exampleService.itemList, person);
    }

    @GetViewAction(path = "/customPath/countUp")
    public IViewContext countUp() {
        counter += 1;
        return render();
    }


    record ActionFormDTO(
            String item
    ) {
    }

    @PostViewAction
    public IViewContext addItem(ActionFormDTO actionFormDTO) {
        exampleService.addItemToList(actionFormDTO.item);
        return render();
    }


    @DeleteViewAction
    public IViewContext deleteItem(Integer id) {
        exampleService.deleteItem(id);
        return render();
    }


    Person person = new Person(
            "Thomas", 23, "Ludwigsburg"
    );

    @PatchViewAction
    public IViewContext savePersonPatch(Person person) {
        this.person = person;
        return render();
    }

    @PutViewAction
    public IViewContext savePersonPut(Person person) {
        this.person = person;
        return render();
    }

    public record Person(
            String name,
            Integer age,
            String location
    ) {
    }


}
