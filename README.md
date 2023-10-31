![image](https://user-images.githubusercontent.com/33346637/235085980-eb16eaa3-ec89-4293-9609-cf651a44f60e.png)

Spring ViewComponent allows you to create typesafe, reusable & encapsulated server rendered view components.

##### Table of Contents  
- [What’s a ViewComponent?](#whats-a-viewcomponent)
- [Render a ViewComponent](#render-a-viewcomponent)
- [Nesting ViewComponents:](#nesting-viewcomponents)
- [Local Development](#local-development)
- [ViewAction: Interactivity with HTMX](#viewaction-interactivity-with-htmx)
- [Installation](#installation)
- [Composing pages from components](#composing-pages-from-components)
- [Serverless components - Spring Cloud Function support](#serverless-components---spring-cloud-function-support)


## What’s a ViewComponent?

Think of ViewComponents as an evolution of the presenter pattern, inspired by [React](https://reactjs.org/docs/react-component.html). 

A ViewComponent is a Spring Bean that defines the context for our Template:

<details open>
    <summary>Java</summary>

```java
@ViewComponent
public class SimpleViewComponent {
    public record SimpleView(String helloWorld) implements ViewContext {
    }

    public SimpleView render() {
        return new SimpleView("Hello World");
    }
}
```
</details>

We define the context by creating a record that implements the ViewContext interface

Next we add the @ViewComponent annotation to a class and define a method that returns the SimpleView record.

<details>
    <summary>Kotlin</summary>

```kotlin
// HomeViewComponent.kt
@ViewComponent
class SimpleViewComponent{
    fun render() = SimpleView("Hello World")

    data class SimpleView(val helloWorld: String) : ViewContext
}
```
</details>

A ViewComponent always need a corresponding HTML Template.
We define the Template in the SimpleViewComponent.[html/jte/kte] in the same package as our ViewComponent Class.

We can use [Thymeleaf](https://thymeleaf.org)

````html 
// SimpleViewComponent.html
<!--/*@thymesVar id="d" type="de.tschuehly.example.thymeleafjava.web.simple.SimpleViewComponent.SimpleView"*/-->
<div th:text="${simpleView.helloWorld()}"></div>
````

or [JTE](https://jte.gg/#5-minutes-example)

```html
// HomeViewComponent.jte
@param de.tschuehly.example.jte.web.simple.SimpleViewComponent.SimpleView simpleView
<div>${simpleView.helloWorld()}</div>
```

or [KTE](https://jte.gg/#5-minutes-example)
```html
@param simpleView: de.tschuehly.kteviewcomponentexample.web.simple.SimpleViewComponent.SimpleView
<div>
    <h2>This is the SimpleViewComponent</h2>
    <div>${simpleView.helloWorld}</div>
</div>
```
[Installation with Gradle](#gradle-installation)

[Thymeleaf Example](https://github.com/tschuehly/spring-view-component/tree/master/examples/thymeleaf-demo)
[JTE Example](https://github.com/tschuehly/spring-view-component/tree/master/examples/jte-demo)

## Render a ViewComponent

We can then call the render method in our Controller
<details open>
    <summary>Java</summary>

```java
@Controller
public class SimpleController {
    private final SimpleViewComponent simpleViewComponent;

    public TestController(SimpleViewComponent simpleViewComponent) {
        this.simpleViewComponent = simpleViewComponent;
    }

    @GetMapping("/")
    ViewContext simple() {
        return simpleViewComponent.render();
    }
}
```
</details>

<details>
    <summary>Kotlin</summary>

```kotlin
// Router.kt
@Controller
class SimpleController(
    private val simpleViewComponent: SimpleViewComponent,
) {

    @GetMapping("/")
    fun simpleComponent() = simpleViewComponent.render()
}
```
</details>

## Examples

If you want to get started right away you can find examples for all possibilites here:
[Examples](/examples/)

## Nesting ViewComponents:

We can nest components by passing a ViewContext as property of our record, 
if we also have it as parameter of our render method we can easily create layouts:

<details open>
    <summary>Java</summary>

```java
@ViewComponent
public
class LayoutViewComponent {

    private record LayoutView(ViewContext nestedViewComponent) implements ViewContext {
    }

    public ViewContext render(ViewContext nestedViewComponent) {
        return new LayoutView(nestedViewComponent);
    }
}
```
</details>
<details >
    <summary>Kotlin</summary>

```kotlin
@ViewComponent
class LayoutViewComponent {
    data class LayoutView(val nestedViewComponent: ViewContext) : ViewContext
    fun render(nestedViewComponent: ViewContext) = LayoutView(nestedViewComponent)

}
```
</details>


### Thymeleaf
In Thymeleaf we render the passed ViewComponent with the `view:component="${viewContext}"` attribute.

```html
<nav>
    This is a navbar
</nav>
<!--/*@thymesVar id="layoutView" type="de.tschuehly.example.thymeleafjava.web.layout.LayoutViewComponent.LayoutView"*/-->
<div view:component="${layoutView.nestedViewComponent()}"></div>
<footer>
    This is a footer
</footer>
```

### JTE

In JTE/KTE we can just call the LayoutView record directly in an expression:
```html
@param layoutView: de.tschuehly.kteviewcomponentexample.web.layout.LayoutViewComponent.LayoutView
<nav>
    This is a Navbar
</nav>
<body>
${layoutView.nestedViewComponent}
</body>
<footer>
    This is a footer
</footer>
```

## Local Development

You can enable hot-reloading of the templates in development:
```properties
spring.view-component.local-development=true
```

## ViewAction: Interactivity with HTMX

With ViewActions you can create interactive ViewComponents based on [htmx](https://htmx.org/) without having to reload the page.

You define a ViewAction inside your Thymeleaf/JTE template with the `view:action` attribute.
```html
// ActionViewComponent.html
<!--/*@thymesVar id="actionView" type="de.tschuehly.example.thymeleafjava.web.action.ActionViewComponent.ActionView"*/-->
<script defer src="https://unpkg.com/htmx.org@1.9.3"></script>
<button view:action="countUp">Default ViewAction [GET]</button>
<h3 th:text="${actionView.counter()}"></h3>
```
Here is the corresponding ViewComponent class that has a `@GetViewAction` annotation on the countUp method.

As you can see the attribute value of the `view:action="countUp"` correlates to the countUp method in our ViewComponent class.

<details open>
    <summary>Java</summary>

```java
@ViewComponent
public class ActionViewComponent {
    Integer counter = 0;

    public record ActionView(Integer counter) implements ActionViewContext {
    }

    public ViewContext render() {
        return new ActionView(counter);
    }

    @GetViewAction(path = "/customPath/countUp")
    public ViewContext countUp() {
        counter += 1;
        return render();
    }
}
```
</details>
<details >
    <summary>Kotlin</summary>

```kotlin
@ViewComponent
class ActionViewComponent {
    data class ActionView(val counter: Int) : ViewContext

    fun render() = ActionView(counter)

    var counter: Int = 0

    @GetViewAction("/customPath/countUp")
    fun countUp(): IViewContext {
        counter += 1
        return render()
    }
}
```
</details>

Behind the scenes at build time Spring ViewComponent parses the template to htmx attributes using an annotation processor.

The hx-get attribute will create a http get request to the `/actionviewcomponent/countup` endpoint that is automatically generated.

The `/actionviewcomponent/countup` endpoint will return the re-rendered ActionViewComponent template. 

The `hx-target="#actionviewcomponent"` attribute will swap the returned HTML to the div with the `id="actionviewcomponent"` that will wrap the view component.


```html
<div id="actionviewcomponent" style="display: contents;">
  <script defer src="https://unpkg.com/htmx.org@1.9.3"></script>
  <h2>ViewAction Get CountUp</h2>
  <button hx-get="/actionviewcomponent/countup" hx-target="#actionviewcomponent">
    Default ViewAction [GET]
  </button>
</div>
```

You can also pass a custom path as annotation parameter: `@PostViewAction("/customPath/addItemAction")`

You can use different ViewAction Annotations that map to the corresponding [htmx ajax methods](https://htmx.org/docs/#ajax): 

- `@GetViewAction`
- `@PostViewAction`
- `@PutViewAction`
- `@PatchViewAction`
- `@DeleteViewAction`

## Installation

If you are using Maven you need to configure the annotation processor like this:

<details>
    <summary>Annotation Processor Configuration</summary>

```xml
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>de.tschuehly</groupId>
                            <artifactId>spring-view-component-core</artifactId>
                            <version>${de.tschuehly.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
</details>


### Thymeleaf:
[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-thymeleaf) on Maven Central

<details open>
    <summary>Gradle</summary>

```kotlin
implementation("de.tschuehly:spring-view-component-thymeleaf:LATEST_VERSION")
annotationProcessor("de.tschuehly:spring-view-component-core:LATEST_VERSION")
```
</details>

<details>
    <summary>Maven</summary>

```xml
<dependency>
    <groupId>de.tschuehly</groupId>
    <artifactId>spring-view-component-core</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
<dependency>
    <groupId>de.tschuehly</groupId>
    <artifactId>spring-view-component-thymeleaf</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
```
</details>



### JTE Dependency
[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-jte) on Maven Central


<details open>
    <summary>Gradle</summary>

```kotlin
implementation("de.tschuehly:spring-view-component-jte:LATEST_VERSION")
annotationProcessor("de.tschuehly:spring-view-component-core:LATEST_VERSION")
```
</details>

<details>
    <summary>Maven</summary>

```xml
<dependency>
    <groupId>de.tschuehly</groupId>
    <artifactId>spring-view-component-core</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
<dependency>
    <groupId>de.tschuehly</groupId>
    <artifactId>spring-view-component-jte</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
```
</details>



## Composing pages from components

**!!! Currently only supported in Thymeleaf !!!**

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


## Serverless components - Spring Cloud Function support

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