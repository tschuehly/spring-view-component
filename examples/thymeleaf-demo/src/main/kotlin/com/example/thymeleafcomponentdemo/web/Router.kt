package com.example.thymeleafcomponentdemo.web

import com.example.thymeleafcomponentdemo.web.home.HomeViewComponent
import com.example.thymeleafcomponentdemo.web.navigation.NavigationViewComponent
import com.example.thymeleafcomponentdemo.web.table.TableViewComponent
import de.tschuehly.spring.viewcomponent.core.ViewContextContainer
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Router(
    private val homeViewComponent: HomeViewComponent,
    private val navigationViewComponent: NavigationViewComponent,
    private val tableViewComponent: TableViewComponent
) {
    @GetMapping("/")
    fun homeComponent(): ViewContext {
        return homeViewComponent.render()
    }

    @GetMapping("/multi-component")
    fun multipleComponent(): ViewContextContainer {
        return ViewContextContainer(
            navigationViewComponent.render(),
            homeViewComponent.render()
        )
    }

    @GetMapping("/table-oob")
    fun tableComponent(): ViewContextContainer {
        return ViewContextContainer(
            navigationViewComponent.render(),
            tableViewComponent.render()
        )
    }
}