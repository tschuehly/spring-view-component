package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.action

import de.tschuehly.spring.viewcomponent.core.*
import de.tschuehly.spring.viewcomponent.core.action.*
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import de.tschuehly.spring.viewcomponent.thymeleaf.application.core.ExampleService

@ViewComponent
class ActionViewComponent(
    val exampleService: ExampleService
) {

    fun render() = ViewContext(
        "itemList" toProperty exampleService.someData,
        "counter" toProperty counter,
        "person" toProperty person
    )

    var counter = 0;

    @GetViewAction("/customPath/countUp")
    fun countUp(): ViewContext{
        counter += 1
        return render()
    }


    class ActionFormDTO(
        val item: String
    )
    @PostViewAction
    fun addItem(actionFormDTO: ActionFormDTO): ViewContext {
        exampleService.addItemToList(actionFormDTO.item)
        return render()
    }


    @DeleteViewAction
    fun deleteItem(id: Int): ViewContext{
        exampleService.deleteItem(id)
        return render()
    }


    var person = Person(
        name = "Thomas", age = 23, location = "Ludwigsburg"
    )

    @PatchViewAction
    fun savePersonPatch(person: Person): ViewContext {
        this.person = person
        return render()
    }
    @PutViewAction
    fun savePersonPut(person: Person): ViewContext {
        this.person = person
        return render()
    }
    class Person(
        val name: String,
        val age: Int,
        val location: String
    )
}