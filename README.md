# thymeleaf-view-component

While developing my side project over the last year with thymeleaf I noticed that the templates you serve with the Controller get quite large. You can split them up by using Thymeleaf fragments. But when you nest multiple fragments and use variable expression it is going to get hard to test.

That's why the Idea of ViewComponents came along. There is a similar Library already available for [Ruby on Rails](https://viewcomponent.org/) which are inspired by react. 

If you want to use the library with [Java and Maven](#java) you can skip ahead

## Kotlin

[Installation with Gradle](#gradle-installation)

[Demo Repository with Kotlin and Gradle](https://github.com/tschuehly/thymeleaf-component-demo)

### Creating a ViewComponent

We just need to add the @ViewComponent annotation to a class and define a render() method that returns a ViewContext. We can pass the properties we want to use in our template.

```kotlin
// HomeViewComponent.kt
@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService
    ) {
    fun render() = ViewContext(
        "helloWorld" toProperty "Hello World",
        "coffee" toProperty exampleService.getCoffee()
    )
}
```

Next we define the HTML in the HomeViewComponent.html in the same package as our ViewComponent Class.

````html 
// HomeViewComponent.html
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <div th:text="${helloWorld}"></div>
    <br>
    <strong th:text="${coffee}"></strong>
</body>
</html>
````

We can then call the render method in our Controller

```kotlin
// Router.kt
@Controller
class Router(
    val homeViewComponent: HomeViewComponent
) {
    @GetMapping("/")
    fun homeComponent(): Any {
        return homeViewComponent.render()
    }
}
```

If we now access the root url path of our spring application we can see that the component renders properly:
![thymeleaf-view-component-example](https://user-images.githubusercontent.com/33346637/222275655-b80a45b0-3a3d-4bd6-909a-1b54ab8cc925.png)


### Nesting components:

We can also embed components to our templates with the attribute `view:component="componentName"`.

```html
<div view:component="navigationViewComponent"></div>
```

When our render method has parameters we can pass them by using the `.render(parameter)` method.
```html
<div view:component="parameterViewComponent.render('Hello World')"></div>
```
### Parameter components:

We can also create components with parameters. We can either use default values when we pass a null value, get a property from a Service or we can throw a custom error.

```kotlin
// ParameterViewComponent.kt
@ViewComponent
class ParameterViewComponent{
    fun render(parameter: String?) = ViewContext(
        "office" toProperty (parameter ?: throw Error("You need to pass in a parameter")),
    )
}
```

```html
// ParameterViewComponent.html
<h2>ParameterComponent:</h2>

<div th:text="${office}"></div>

```

This enables us to define the properties for our ParameterViewComponent in the HomeViewComponent:

```kotlin
// HomeViewComponent.kt
@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService,
) {
    fun render() = ViewContext(
            "helloWorld" toProperty exampleService.getHelloWorld(),
            "office" toProperty exampleService.getOfficeHours()
        )
}
```
```html
// HomeViewComponent.html
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<body>

<div th:text="${helloWorld}"></div>

<div view:component="parameterViewComponent.render(office)"></div>

</body>
</html>
```


If we now access the root url path of our spring application we can see that the parameter component renders properly:

![viewcomponent-parameter](https://user-images.githubusercontent.com/33346637/222275688-7f301ff7-4a69-4062-ae69-1dd6c9983a7a.png)


### Gradle Installation

Add this snippet to your build.gradle.kts:
```kotlin
// build.gradle.kts
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.tschuehly:thymeleaf-view-component:0.3.1")
}
sourceSets {
    main {
        resources {
            srcDir("src/main/kotlin")
            exclude("**/*.kt")
        }
    }
}
```

## Java

[Installation with Maven](#maven-installation)

[Demo Repository with Java and Maven](https://github.com/tschuehly/view-component-java-maven-demo/tree/master)

### Creating a ViewComponent

We just need to add the @ViewComponent annotation to a class and define a render() method that returns a ViewContext. We can pass the properties we want to use in our template.

```java
// HomeViewComponent.java
@ViewComponent
public class HomeViewComponent {
    private final ExampleService exampleService;

    public HomeViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public ViewContext render() {
        return new ViewContext(
                ViewProperty.of("helloWorld", "Hello World"),
                ViewProperty.of("coffee", exampleService.getCoffee())
        );
    }
}

```

Next we define the HTML in the HomeViewComponent.html in the same package as our ViewComponent Class.

````html 
// HomeViewComponent.html
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <div th:text="${helloWorld}"></div>
    <br>
    <strong th:text="${coffee}"></strong>
</body>
</html>
````

We can then call the render method in our Controller

```java
// Router.java
@Controller
public class Router {
    private final HomeViewComponent HomeViewComponent;

    public Router(HomeViewComponent HomeViewComponent) {
        this.HomeViewComponent = HomeViewComponent;
    }

    @GetMapping("/")
    ViewContext homeView(){
        return HomeViewComponent.render();
    }
}
```

If we now access the root url path of our spring application we can see that the component renders properly:
![thymeleaf-view-component-example](https://user-images.githubusercontent.com/33346637/222275655-b80a45b0-3a3d-4bd6-909a-1b54ab8cc925.png)


### Nesting components:

We can also embed components to our templates with the attribute `view:component="componentName"`.

```html

<div view:component="navigationViewComponent"></div>
```

When our render method has parameters we can pass them by using the `.render(parameter)` method.
```html

<div view:component="parameterViewComponent.render('Hello World')"></div>
```

### Parameter components:

We can also create components with parameters. We can either use default values when we pass a null value, get a property from a Service or we can throw a custom error.

```java
@ViewComponent
public class ParameterViewComponent {
    public ViewContext render(String parameter) throws Exception {
        if (parameter == null) {
            throw new Exception("You need to pass in a parameter");
        }
        return new ViewContext(
                ViewProperty.of("office", parameter)
                );
    }
}

```

```html
// ParameterViewComponent.html
<h2>ParameterComponent:</h2>

<div th:text="${office}"></div>

```

This enables us to define the properties for our ParameterViewComponent in the HomeViewComponent:

```java
// HomeViewComponent.java
@ViewComponent
public class HomeViewComponent {
    private final ExampleService exampleService;

    public HomeViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    public ViewContext render() {
        return new ViewContext(
                ViewProperty.of("helloWorld", "Hello World"),
                ViewProperty.of("coffee", exampleService.getCoffee()),
                ViewProperty.of("office", exampleService.getOfficeHours())
        );
    }
}

```
```html
// HomeViewComponent.html
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<body>

<div th:text="${helloWorld}"></div>

<div view:component="parameterViewComponent.render(office)"></div>
</body>
</html>
```

If we now access the root url path of our spring application we can see that the parameter component renders properly:

![viewcomponent-parameter](https://user-images.githubusercontent.com/33346637/222275688-7f301ff7-4a69-4062-ae69-1dd6c9983a7a.png)


### Maven Installation

Add this to your pom.xml:
```xml
<project>
    <dependencies>
        <dependency>
            <groupId>de.github.tschuehly</groupId>
            <artifactId>thymeleaf-view-component</artifactId>
            <version>0.3.1</version>
        </dependency>
    </dependencies>
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.html</include>
                </includes>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.3.0</version>
            </plugin>
        </plugins>
    </build>
    <repositories>
        <repository>
            <id>Jitpack</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
</project>
```
