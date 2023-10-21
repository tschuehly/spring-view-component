package de.tschuehly.example.thymeleafjava.web.action;

public class Person
{

  public String getName() {
    return name;
  }

  public Integer getAge() {
    return age;
  }

  public String getLocation() {
    return location;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  String name;
  Integer age;
  String location;

  public Person(String name, Integer age, String location) {
    this.name = name;
    this.age = age;
    this.location = location;
  }

}
