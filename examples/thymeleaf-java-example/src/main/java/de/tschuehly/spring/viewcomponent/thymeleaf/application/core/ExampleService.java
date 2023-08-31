package de.tschuehly.spring.viewcomponent.thymeleaf.application.core;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.NoSuchElementException;

@Service
public class ExampleService {
    Integer dataIndex = 0;
    public HashMap<Integer, String> itemList = new HashMap<>();

    public String getHelloWorld() {
        return "Hello World";
    }

    public void addItemToList(String item) {
        itemList.put(dataIndex, item);
        dataIndex += 1;
    }

    public void deleteItem(Integer id) {
        if (itemList.get(id) == null) {
            throw new NoSuchElementException("No Element with Id: " + id + " found");
        }
        itemList.remove(id);
    }

}
