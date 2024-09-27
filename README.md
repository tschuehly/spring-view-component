![image](https://user-images.githubusercontent.com/33346637/235085980-eb16eaa3-ec89-4293-9609-cf651a44f60e.png)

Spring ViewComponent allows you to create typesafe, reusable & encapsulated server-rendered UI components.

##### Table of Contents

- [What’s a ViewComponent?](#whats-a-viewcomponent)
- [Render a ViewComponent](#render-a-viewcomponent)
- [Nesting ViewComponents:](#nesting-viewcomponents)
- [Local Development Configuration](#local-development)
- [Production Configuration](#production-configuration)
- [Installation](#installation)
- [Technical Implementation](#technical-implementation)

## What’s a ViewComponent?

ViewComponents consolidate the logic needed for a template into a single class,
resulting in a cohesive object that is easy to understand. 
([Source: ViewComponent for Rails](https://viewcomponent.org/))

A Spring ViewComponent is a Spring Bean that defines the context for our Template:

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

Next, we add the `@ViewComponent` annotation to a class and define a method that returns the `SimpleView` record.

<details>
    <summary>Kotlin</summary>

```kotlin
// SimpleViewComponent.kt
@ViewComponent
class SimpleViewComponent {
    fun render() = SimpleView("Hello World")

    data class SimpleView(val helloWorld: String) : ViewContext
}
```

</details>

A ViewComponent always needs a corresponding HTML Template.
We define the Template in the SimpleViewComponent.[html/jte/kte] In the same package as our ViewComponent class.

We can use [Thymeleaf](https://thymeleaf.org)

````html 
// SimpleViewComponent.html
<!--/*@thymesVar id="d" type="de.tschuehly.example.thymeleafjava.web.simple.SimpleViewComponent.SimpleView"*/-->
<div th:text="${simpleView.helloWorld()}"></div>
````

or [JTE](https://jte.gg/#5-minutes-example)

```html
// SimpleViewComponent.jte
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

## Render a ViewComponent

We can then call the render method in our controller to render the template.
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

If you want to get started right away you can find examples for all possible language combinations here:
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

### JTE / KTE

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

## Local Development Configuration

You can enable hot-reloading of the templates in development, you need to have Spring Boot DevTools as a dependency.

```properties
spring.view-component.local-development=true
```

## Installation

### Thymeleaf:

[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-thymeleaf) on Maven Central

<details open>
    <summary>Gradle</summary>

```kotlin
implementation("de.tschuehly:spring-view-component-thymeleaf:0.8.3")
sourceSets {
    main {
        resources {
            srcDir("src/main/java")
            exclude("**/*.java")
        }
    }

}
```
</details>

<details>
    <summary>Maven</summary>

```xml
<project>
  <dependencies>
    <dependency>
      <groupId>de.tschuehly</groupId>
      <artifactId>spring-view-component-thymeleaf</artifactId>
      <version>0.8.3</version>
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
      <resource>
        <directory>src/main/resources</directory>
      </resource>
    </resources>
    <plugins>
      <plugin>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.3.0</version>
      </plugin>
    </plugins>
  </build>
</project>
```

</details>

### JTE

[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-jte) on Maven Central


<details open>
    <summary>Gradle</summary>

```kotlin
plugins {
    id("gg.jte.gradle") version("3.1.12")
}

implementation("de.tschuehly:spring-view-component-jte:0.8.3")

jte{
    generate()
    sourceDirectory.set(kotlin.io.path.Path("src/main/java"))
}
```

</details>

<details>
    <summary>Maven</summary>

```xml
<project >
  <dependencies>
    <dependency>
      <groupId>de.tschuehly</groupId>
      <artifactId>spring-view-component-jte</artifactId>
      <version>0.8.3</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>gg.jte</groupId>
        <artifactId>jte-maven-plugin</artifactId>
        <version>3.1.12</version>
        <configuration>
          <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
          <contentType>Html</contentType>
        </configuration>
        <executions>
          <execution>
            <phase>generate-sources</phase>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

</details>

### KTE

[LATEST_VERSION](https://central.sonatype.com/artifact/de.tschuehly/spring-view-component-kte) on Maven Central


<details open>
    <summary>Gradle</summary>

```kotlin

plugins {
    id("gg.jte.gradle") version ("3.1.12")
}

dependencies {
    implementation("de.tschuehly:spring-view-component-kte:0.8.3")
}

jte {
    generate()
    sourceDirectory.set(kotlin.io.path.Path("src/main/kotlin"))
}
```

</details>

## Technical Implementation

Spring ViewComponent wraps the Spring MVC container using an AspectJ Aspect and automatically resolves the template and puts the ViewContext in the ModelAndViewContainer

![image](https://github.com/tschuehly/spring-view-component/assets/33346637/ad2f2517-7eab-4b07-9249-59aeaae1e778)
