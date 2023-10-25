package de.tschuehly.example.thymeleafkotlin.web.action

import de.tschuehly.example.thymeleafkotlin.core.ExampleService
import de.tschuehly.spring.viewcomponent.core.IViewContext
import de.tschuehly.spring.viewcomponent.core.action.*
import de.tschuehly.spring.viewcomponent.core.component.ViewComponent
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext

@ViewComponent
class ActionViewComponent(
    val exampleService: ExampleService
) {
    data class ActionView(val itemList: MutableMap<Int, String>, val counter: Int, val person: Person) : ViewContext

    fun render() = ActionView(exampleService.itemList, counter, person)

    var counter: Int = 0

    @GetViewAction("/customPath/countUp")
    fun countUp(): ViewContext {
        counter += 1
        return render()
    }


    class ActionFormDTO(
        val item: String
    )

    @PostViewAction
    fun addItem(actionFormDTO: ActionFormDTO): IViewContext {
        exampleService.addItemToList(actionFormDTO.item)
        return render()
    }


    @DeleteViewAction
    fun deleteItem(id: Int): IViewContext {
        exampleService.deleteItem(id)
        return render()
    }


    var person = Person(
        name = "Thomas", age = 23, location = "Ludwigsburg"
    )

    @PatchViewAction
    fun savePersonPatch(person: Person): IViewContext {
        this.person = person
        return render()
    }

    @PutViewAction
    fun savePersonPut(person: Person): IViewContext {
        this.person = person
        return render()
    }
    class PersonDTO(
        val person: Person?
    )

    class Person(
        val name: String?,
        val age: Int?,
        val location: String?
    )
}