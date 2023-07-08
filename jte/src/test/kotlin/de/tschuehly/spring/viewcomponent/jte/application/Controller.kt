package de.tschuehly.spring.viewcomponent.jte.application

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {

    @GetMapping("/test")
    fun test() = "Hello World"
}