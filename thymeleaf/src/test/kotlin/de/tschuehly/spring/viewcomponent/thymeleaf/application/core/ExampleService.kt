package de.tschuehly.spring.viewcomponent.thymeleaf.application.core

import org.springframework.stereotype.Service

@Service
class ExampleService {
    val someData = mutableListOf<String>()

    fun getHelloWorld(): String{
        return "Hello World"
    }

    fun addItemToList(item: String): MutableList<String> {
        someData.add(item)
        return someData
    }


}