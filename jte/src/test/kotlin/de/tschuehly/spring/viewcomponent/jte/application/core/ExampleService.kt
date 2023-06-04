package de.tschuehly.spring.viewcomponent.jte.application.core

import org.springframework.stereotype.Service

@Service
class ExampleService {
    fun getHelloWorld(): String{
        return "Hello World"
    }
}