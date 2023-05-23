package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.action

import de.tschuehly.spring.viewcomponent.core.ViewAction
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import de.tschuehly.spring.viewcomponent.thymeleaf.application.core.ExampleService

@ViewComponent
class ActionViewComponent(
    val exampleService: ExampleService
) {

    fun render() = ViewContext(
        "helloThere" toProperty "ViewAction is working!",
        "itemList" toProperty exampleService.someData
    )

    @ViewAction
    fun addItem(actionFormDTO: ActionFormDTO): ViewContext {
        exampleService.addItemToList(actionFormDTO.item)
        return render()
    }

    class ActionFormDTO(
        val item: String
    )
}