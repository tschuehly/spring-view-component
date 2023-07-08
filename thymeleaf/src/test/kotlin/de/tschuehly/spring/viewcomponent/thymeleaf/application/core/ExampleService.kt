package de.tschuehly.spring.viewcomponent.thymeleaf.application.core

import org.springframework.stereotype.Service
import java.util.NoSuchElementException

@Service
class ExampleService {
    var dataIndex = 0
    val someData = mutableMapOf<Int, String>()

    fun getHelloWorld(): String {
        return "Hello World"
    }

    fun addItemToList(item: String){
        someData[dataIndex] = item
        dataIndex += 1
    }

    fun deleteItem(id: Int) {
        someData.remove(id) ?: throw NoSuchElementException("No Element with Id: $id found")
    }


}