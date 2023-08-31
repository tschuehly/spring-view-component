package de.tschuehly.jteviewcomponentdemo.core

import org.springframework.stereotype.Service
import java.util.NoSuchElementException

@Service
class ExampleService {
    var dataIndex = 0
    val itemList = mutableMapOf<Int, String>()

    fun getHelloWorld(): String {
        return "Hello World"
    }

    fun addItemToList(item: String){
        itemList[dataIndex] = item
        dataIndex += 1
    }

    fun deleteItem(id: Int) {
        itemList.remove(id) ?: throw NoSuchElementException("No Element with Id: $id found")
    }


}