# thymeleaf-view-component

While developing my side project over the last year with thymeleaf I noticed that the templates you serve with the Controller get quite large. You can split them up by using Thymeleaf fragments. But when you nest multiple fragments and use variable expression it is going to get hard to test.

That's why the Idea of ViewComponents came along. There is a similar Library already available for [Ruby on Rails](https://viewcomponent.org/) which are inspired by react. 

[How to install thymeleaf-view-component](#installation)

### Creating a ViewComponent

We just need to add the @ViewComponent annotation to a class and define a render() method that returns a ViewComponentContext. We can pass the properties we want to use in our template.

```kotlin
// HomeViewComponent.kt
@ViewComponent
class HomeViewComponent(
    private val exampleService: ExampleService
    ) {
    fun render() = ViewComponentContext(
        "helloWorld" to "Hello World",
        "coffee" to exampleService.getCoffee()
    )
}
```

Next we define the HTML in the HomeViewComponent.html in the same package as our ViewComponent Class.

````html 
// HomeViewComponent.html
<div th:text="${helloWorld}"></div>
<strong th:text="${coffee}"></strong>
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

We can also embed components to our templates, either with expression inlining `[(${{}})]` or with the `th:utext=${{}}` property. It is important to use double curly bracelets `{{}}`.

```html
<div>
    [(${{@navigationViewComponent.render()}})]
</div>
```

```html
<div th:utext="${{@navigationViewComponent.render()}}"></div>
```
### Parameter components:

We can also create components with parameters. We can either use default values when we pass a null value, get a property from a Service or we can throw a custom error.

```kotlin
// ParameterViewComponent.kt
@ViewComponent
class ParameterViewComponent(
    private val exampleService: ExampleService
){
    fun render(parameter: String?) = ViewComponentContext(
        "office" to (parameter ?: throw Error("You need to pass in a parameter")),
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
    fun render() = ViewComponentContext(
            "helloWorld" to exampleService.getHelloWorld(),
            "office" to exampleService.getOfficeHours()
        )
}
```
```html
// HomeViewComponent.html
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
<body>

<div th:text="${helloWorld}"></div>

<div th:utext="${{@parameterViewComponent.render(office)}}"></div>

</body>
</html>
```


If we now access the root url path of our spring application we can see that the parameter component renders properly:

![viewcomponent-parameter](https://user-images.githubusercontent.com/33346637/222275688-7f301ff7-4a69-4062-ae69-1dd6c9983a7a.png)


### Installation

Add this snippet to your build.gradle.kts:
```kotlin
// build.gradle.kts
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.tschuehly:thymeleaf-view-component:0.1.3")
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


### Sources

You can find the source code of the library here: [thymeleaf-view-component](https://github.com/tschuehly/thymeleaf-view-component)
