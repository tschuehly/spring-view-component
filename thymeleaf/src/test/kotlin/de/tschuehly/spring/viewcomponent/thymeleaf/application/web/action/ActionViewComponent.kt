package de.tschuehly.spring.viewcomponent.thymeleaf.application.web.action

import de.tschuehly.spring.viewcomponent.core.*
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import de.tschuehly.spring.viewcomponent.thymeleaf.application.core.ExampleService
import org.springframework.web.bind.annotation.RequestMethod
import javax.swing.text.View

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

    @GetViewAction("/helloWim/countUp")
    fun countUp(): ViewContext{
        counter += 1
        return render()
    }

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

    class ActionFormDTO(
        val item: String
    )
}