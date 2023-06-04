package de.tschuehly.spring.viewcomponent.thymeleaf.application.core

import org.springframework.stereotype.Service

@Service
class ExampleService {
    fun getHelloWorld(): String{
        return "Hello World"
    }
}