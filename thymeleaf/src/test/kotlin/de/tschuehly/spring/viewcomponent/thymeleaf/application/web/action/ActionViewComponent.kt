package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.action

import de.tschuehly.spring.viewcomponent.core.PostViewAction
import de.tschuehly.spring.viewcomponent.core.ViewComponent
import de.tschuehly.spring.viewcomponent.core.toProperty
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import de.tschuehly.spring.viewcomponent.thymeleaf.application.core.ExampleService
import org.springframework.web.bind.annotation.RequestMethod

@ViewComponent
class ActionViewComponent(
    val exampleService: ExampleService
) {
    var counter = 0;

    fun render() = ViewContext(
        "helloThere" toProperty "ViewAction is working!",
        "itemList" toProperty exampleService.someData,
        "counter" toProperty counter
    )

    @PostViewAction
    fun addItem(actionFormDTO: ActionFormDTO): ViewContext {
        exampleService.addItemToList(actionFormDTO.item)
        return render()
    }
// TODO: Get doesn't work yet
    @PostViewAction
    fun countUp(): ViewContext{
        counter += 1
        return render()
    }

    class ActionFormDTO(
        val item: String
    )
}