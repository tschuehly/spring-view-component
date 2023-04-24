# Spring ViewComponent

Spring ViewComponent allows you to create typesafe, reusable, testable & encapsulated server rendered view components.

GitHub developed a similar Library for [Ruby on Rails](https://viewcomponent.org/) to ease the development the very site
you are on right now.

## What’s a ViewComponent?

Think of ViewComponents as an evolution of the presenter pattern, inspired by React. 

A ViewComponent is a Spring Bean that defines the context for our Template: 

```kotlin
// HomeViewComponent.kt
@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService
) {
    fun render() = ViewContext(
        "helloWorld" toProperty exampleService.getCoffee(),
    )
}
```

And te HTML Template, you can use [Thymeleaf](https://thymeleaf.org)

````html 
// HomeViewComponent.html
<div th:text="${helloWorld}"></div>
````

or you can use [JTE](https://github.com/casid/jte)

```html
// HomeViewComponent.jte
@param String helloWorld
<div>${helloWorld}</div>
```

# Usage

If you want to use the library with [Java and Maven](#java) you can skip ahead

## Kotlin

[Installation with Gradle](#gradle-installation)

[Demo Repository with Kotlin and Gradle](https://github.com/tschuehly/thymeleaf-component-demo)

### Creating a ViewComponent

We just need to add the @ViewComponent annotation to a class and define a render() method that returns a ViewContext. We
can pass the properties we want to use in our template.

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

Next we define the HTML in the HomeViewComponent.[jte/html] in the same package as our ViewComponent Class.

#### Thymeleaf
````html 
// HomeViewComponent.html
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div>${helloWorld}</div>
<br>
<strong>${coffee}</strong>
</body>
</html>
````
#### JTE
````html 
// HomeViewComponent.jte
<html>
<body>
<div>${helloWorld}</div>
<br>
<strong>${coffee}</strong>
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
    fun homeComponent() = homeViewComponent.render()
}
```

If we now access the root url path of our spring application we can see that the component renders properly:
![thymeleaf-view-component-example](https://user-images.githubusercontent.com/33346637/222275655-b80a45b0-3a3d-4bd6-909a-1b54ab8cc925.png)

### Nesting components:

We can also embed components to our templates:

#### Thymeleaf
We can nest components with the attribute `view:component="componentName"`.

```html
<div view:component="navigationViewComponent"></div>
```

When our render method has parameters we can pass them by using the `.render(parameter)` method.

```html

<div view:component="parameterViewComponent.render('Hello World')"></div>
```

#### JTE

We can nest component by passing the component to our template as a ViewProperty:

```kotlin
// HomeViewComponent.kt
@ViewComponent
class HomeViewComponent(
    private val exampleViewComponent: ExampleViewComponent,
    private val parameterViewComponent: ParameterViewComponent
) {
    fun render() = JteViewContext(
        "exampleViewComponent" toProperty exampleViewComponent,
        "parameterViewComponent" toProperty parameterViewComponent
    )
}
```
We can then invoke the render method on the ViewComponent, both with parameter or without parameter
```html
@import de.tschuehly.jteviewcomponentdemo.web.example.ExampleViewComponent
@import de.tschuehly.jteviewcomponentdemo.web.para.ParameterViewComponent

@param ExampleViewComponent exampleViewComponent
@param ParameterViewComponent parameterViewComponent

${exampleViewComponent.render()}
${parameterViewComponent.render("This is a parameter")}
```

### Parameter components:

We can also create components with parameters. We can either use default values when we pass a null value, get a
property from a Service or we can throw a custom error.

```kotlin
// ParameterViewComponent.kt
@ViewComponent
class ParameterViewComponent {
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
    fun render() = ViewContext( // JteViewContext with JTE
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

### Composing pages from components

**Currently only supported in Thymeleaf !!!**

If you want to compose a page/response from multiple components you can use the `ViewContextContainer` as response in
your controller, this can be used for [htmx out of band responses](https://htmx.org/examples/update-other-content/#oob).

```kotlin
@Controller
class Router(
    private val homeViewComponent: HomeViewComponent,
    private val navigationViewComponent: NavigationViewComponent,
) {

    @GetMapping("/multi-component")
    fun multipleComponent() = ViewContextContainer(
        navigationViewComponent.render(),
        homeViewComponent.render()
    )
}
```

### Serverless components - Spring Cloud Function support

**Currently only supported in Thymeleaf !!!**

If you want to deploy your application on a serverless platform such as AWS Lambda or Azure Functions you can easily do
that with the Spring Cloud Function support.

Just add the dependency `implementation("org.springframework.cloud:spring-cloud-function-context")` to your
build.gradle.kts.

Create a @ViewComponent that implements the functional interface `Supplier<ViewContext>`. Instead of the render()
function we will now override the get method of the Supplier interface.

If you start your application the component should be automatically rendered on http://localhost:8080

```kotlin
@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService,
) : Supplier<ViewContext> {
    override fun get() = ViewContext(
        "helloWorld" toProperty exampleService.getHelloWorld(),
        "coffee" toProperty exampleService.getCoffee()
    )
}
```
### Gradle Installation

Add this snippet to your build.gradle.kts:
```kotlin
// build.gradle.kts
sourceSets {
    main {
        resources {
            srcDir("src/main/kotlin")
            exclude("**/*.kt")
        }
    }
}
```
#### Thymeleaf Dependency

[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-thymeleaf) on Maven Central

```kotlin
// build.gradle.kts
dependencies {
    implementation("de.tschuehly:spring-view-component-thymeleaf:LATEST_VERSION")
}
```
#### JTE Dependency
[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-jte) on Maven Central

```kotlin
// build.gradle.kts
dependencies {
    implementation("de.tschuehly:spring-view-component-jte:LATEST_VERSION")
}
```



## Java

**The Templates don't differ with Java following are java code snippet examples you can use with the templates defined above in the [Kotlin](#kotlin) Part**

[Installation with Maven](#maven-installation)

[Demo Repository with Java and Maven](https://github.com/tschuehly/view-component-java-maven-demo/tree/master)

### Creating a ViewComponent

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


```java
// Router.java
@Controller
public class Router {
    private final HomeViewComponent homeViewComponent;

    public Router(HomeViewComponent homeViewComponent) {
        this.HomeViewComponent = homeViewComponent;
    }

    @GetMapping("/")
    ViewContext homeView() {
        return homeViewComponent.render();
    }
}
```

### Parameter components:

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

### Composing pages from components

```java
// Router.java
@Controller
public class Router {
    private final NavigationViewComponent navigationViewComponent;
    private final TableViewComponent tableViewComponent;

    public Router(NavigationViewComponent navigationViewComponent, TableViewComponent tableViewComponent) {
        this.navigationViewComponent = navigationViewComponent;
        this.tableViewComponent = tableViewComponent;
    }

    @GetMapping("/multi-component")
    ViewContextContainer multipleComponent() {
        return new ViewContextContainer(
                this.navigationViewComponent.render(),
                this.tableViewComponent.render()
        );
    }
}
```

### Serverless components - Spring Cloud Function support

```java
// HomeViewComponent.java
@ViewComponent
public class HomeViewComponent implements Supplier<ViewContext> {
    private final ExampleService exampleService;

    public HomeViewComponent(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @Override
    public ViewContext get() {
        return new ViewContext(
                ViewProperty.of("helloWorld", "Hello World"),
                ViewProperty.of("coffee", exampleService.getCoffee()),
                ViewProperty.of("office", exampleService.getOfficeHours())
        );
    }
}
```

### Maven Installation

Add this to your pom.xml:

```xml

<project>
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.html</include>
                    <include>**/*.jte</include>
                </includes>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>5
                <version>3.3.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```


#### Thymeleaf Dependency
```xml
<dependency>
    <groupId>de.tschuehly</groupId>
    <artifactId>spring-view-component-thymeleaf</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
```
[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-thymeleaf) on Maven Central

#### JTE Dependency
[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-jte) on Maven Central

```xml
<dependency>
    <groupId>de.tschuehly</groupId>
    <artifactId>spring-view-component-jte</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
```

## General Configuration


### Local Development

To enable live reload of the components on each save without rebuilding the application add this configuration:

```properties
spring.view-component.localDevelopment=true
```
