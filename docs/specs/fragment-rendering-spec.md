# Fragment Rendering Specification

**Version:** 2.0-DRAFT
**Date:** 2026-01-01
**Author:** Spring View Component Team

## Table of Contents

1. [Overview](#overview)
2. [Problem Statement](#problem-statement)
3. [Goals](#goals)
4. [Non-Goals](#non-goals)
5. [Proposed Solution](#proposed-solution)
6. [Technical Design](#technical-design)
7. [Examples](#examples)
8. [Implementation Considerations](#implementation-considerations)
9. [Migration Path](#migration-path)
10. [Alternatives Considered](#alternatives-considered)
11. [Open Questions](#open-questions)

---

## Overview

This specification proposes adding **fragment rendering** capabilities to Spring View Component. The goal is to enable:

- **Multiple ViewContext implementations** for a single ViewComponent
- **Presence-based conditional rendering** within templates
- **Flexible composition** with `MultiViewContext`
- **Reduced code duplication** for components with variations

Two complementary patterns are introduced:

1. **Type-Based Variants** - One ViewContext selected, one fragment renders (e.g., button variants)
2. **Composition with MultiViewContext** - Multiple ViewContexts, multiple fragments render (e.g., page layouts)

---

## Problem Statement

### Current Limitations

1. **One ViewContext per ViewComponent**: Each ViewComponent currently supports one ViewContext implementation, leading to:
   - Code duplication when creating similar components with slight variations
   - Proliferation of ViewComponent classes for related functionality
   - Difficulty managing component families (e.g., buttons: primary, secondary, danger)

2. **Optional Sections Require Optional<>**: Conditional sections need verbose Optional handling:
   ```java
   record Page(ViewContext header, Optional<ViewContext> footer) implements ViewContext {}
   ```

   Template:
   ```html
   <footer th:if="${page.footer.isPresent()}">
       <div view:component="${page.footer.get()}"></div>
   </footer>
   ```

3. **No Template-Level Conditional Rendering**: Conditional logic must be:
   - Implemented in template engine syntax (`th:if`, JTE conditionals)
   - Difficult to type-check at compile time

### Real-World Use Cases

#### Use Case 1: Button Component with Variants

**Current approach:**
```java
@ViewComponent
public class PrimaryButtonComponent { ... }

@ViewComponent
public class SecondaryButtonComponent { ... }

@ViewComponent
public class DangerButtonComponent { ... }
```

**Desired:** One `ButtonComponent` with multiple ViewContext types for variants.

#### Use Case 2: Page Layout with Optional Sections

**Current approach:**
```java
record Page(
    ViewContext header,
    ViewContext content,
    Optional<ViewContext> footer  // Verbose!
) implements ViewContext {}
```

**Desired:** Clean composition without `Optional<>`.

---

## Goals

1. **Enable Multiple ViewContext Implementations** - Allow a single ViewComponent to return different ViewContext types
2. **Presence-Based Fragment Selection** - Render fragments based on which ViewContexts are present in the model
3. **Clean Composition API** - `MultiViewContext` for combining multiple sections
4. **Type Safety** - Leverage Java/Kotlin type system
5. **Template Engine Agnostic** - Support JTE, KTE, and Thymeleaf
6. **Backward Compatibility** - Existing ViewComponents continue to work unchanged
7. **Runtime Validation** - Clear error messages when constraints are violated

---

## Non-Goals

1. **Cross-Component Fragments** - ViewContexts from different components in one MultiViewContext (use nested components)
2. **Template Scanning/Startup Validation** - Runtime validation is sufficient
3. **Compile-Time Validation** - Annotation processor complexity not justified
4. **Fragment Inheritance** - Can be added in future if needed
5. **Fragment Parameters** - Use ViewContext properties instead

---

## Proposed Solution

### Pattern 1: Type-Based Variants

**One ViewContext in model → One fragment renders**

```java
@ViewComponent
public class ButtonComponent {

    public record PrimaryButton(String label, String action) implements ViewContext {}
    public record SecondaryButton(String label, String action) implements ViewContext {}
    public record DangerButton(String label, String action, String confirmMsg) implements ViewContext {}

    public PrimaryButton primary(String label, String action) {
        return new PrimaryButton(label, action);
    }

    public SecondaryButton secondary(String label, String action) {
        return new SecondaryButton(label, action);
    }

    public DangerButton danger(String label, String action, String confirmMsg) {
        return new DangerButton(label, action, confirmMsg);
    }
}
```

**Thymeleaf Template:**
```html
<!-- ButtonComponent.html -->

<button view:context="PrimaryButton" class="btn btn-primary"
        th:attr="data-action=${primaryButton.action}">
    <span th:text="${primaryButton.label}">Primary</span>
</button>

<button view:context="SecondaryButton" class="btn btn-secondary"
        th:attr="data-action=${secondaryButton.action}">
    <span th:text="${secondaryButton.label}">Secondary</span>
</button>

<button view:context="DangerButton" class="btn btn-danger"
        th:attr="data-action=${dangerButton.action}"
        th:onclick="|confirm('${dangerButton.confirmMsg}')|">
    <span th:text="${dangerButton.label}">Danger</span>
</button>
```

**Rendering:**
- If controller returns `PrimaryButton` → only first fragment renders
- If controller returns `DangerButton` → only third fragment renders

### Pattern 2: Composition with MultiViewContext

**Multiple ViewContexts in model → Multiple fragments render**

```java
@ViewComponent
public class PageComponent {

    public record Header(String title) implements ViewContext {}
    public record Content(String body) implements ViewContext {}
    public record Footer(String text) implements ViewContext {}

    public ViewContext withFooter(String title, String body, String footer) {
        return MultiViewContext.of(
            new Header(title),
            new Content(body),
            new Footer(footer)
        );
    }

    public ViewContext withoutFooter(String title, String body) {
        return MultiViewContext.of(
            new Header(title),
            new Content(body)
            // No Footer - won't render!
        );
    }
}
```

**Template:**
```html
<!-- PageComponent.html -->

<header view:context="Header">
    <h1 th:text="${header.title}">Title</h1>
</header>

<main view:context="Content">
    <p th:text="${content.body}">Content</p>
</main>

<footer view:context="Footer">
    <small th:text="${footer.text}">Footer</small>
</footer>
```

**Rendering:**
- `withFooter()` → all three fragments render
- `withoutFooter()` → only header and content render (footer removed)

### Pattern 3: Shared Properties with Sealed Interfaces

**Best practice for fragments with common properties:**

```java
@ViewComponent
public class AlertComponent {

    // Base interface for shared properties
    sealed interface Alert extends ViewContext {
        String message();
    }

    public record InfoAlert(String message) implements Alert {}
    public record WarningAlert(String message, String details) implements Alert {}
    public record ErrorAlert(String message, String stackTrace) implements Alert {}

    public InfoAlert info(String message) {
        return new InfoAlert(message);
    }

    public WarningAlert warning(String message, String details) {
        return new WarningAlert(message, details);
    }

    public ErrorAlert error(String message, String stackTrace) {
        return new ErrorAlert(message, stackTrace);
    }
}
```

**Template:**
```html
<!--/*@thymesVar id="alert" type="de.example.AlertComponent.Alert"*/-->
<!--/*@thymesVar id="warningAlert" type="de.example.AlertComponent.WarningAlert"*/-->
<!--/*@thymesVar id="errorAlert" type="de.example.AlertComponent.ErrorAlert"*/-->

<div view:context="InfoAlert" class="alert alert-info">
    <span th:text="${alert.message}">Info</span>
</div>

<div view:context="WarningAlert" class="alert alert-warning">
    <span th:text="${alert.message}">Warning</span>
    <pre th:text="${warningAlert.details}">Details</pre>
</div>

<div view:context="ErrorAlert" class="alert alert-error">
    <span th:text="${alert.message}">Error</span>
    <pre th:text="${errorAlert.stackTrace}">Stack</pre>
</div>
```

**Benefits:**
- Shared properties accessible via `${alert.message}` (works in all fragments)
- Specific properties via `${errorAlert.stackTrace}` (type-safe)
- Sealed interface enables exhaustiveness checking (Java 17+)

---

## Technical Design

### 1. Core: MultiViewContext

Framework-provided class for composition:

```java
package de.tschuehly.spring.viewcomponent.core;

public final class MultiViewContext implements IViewContext {
    private final List<IViewContext> contexts;

    private MultiViewContext(IViewContext... contexts) {
        this.contexts = List.of(contexts);
    }

    /**
     * Creates a MultiViewContext from multiple ViewContext instances.
     *
     * @param contexts ViewContext instances (nulls are filtered out)
     * @return MultiViewContext containing all non-null contexts
     * @throws ViewComponentException if contexts are from different components
     */
    public static MultiViewContext of(IViewContext... contexts) {
        // Filter out nulls for easier conditional composition
        IViewContext[] filtered = Arrays.stream(contexts)
            .filter(Objects::nonNull)
            .toArray(IViewContext[]::new);

        validateSameComponent(filtered);
        return new MultiViewContext(filtered);
    }

    public List<IViewContext> getContexts() {
        return contexts;
    }

    private static void validateSameComponent(IViewContext... contexts) {
        if (contexts.length == 0) {
            throw new ViewComponentException(
                "MultiViewContext requires at least one non-null ViewContext"
            );
        }

        Class<?> expectedComponent = contexts[0].getClass().getEnclosingClass();

        if (expectedComponent == null) {
            throw new ViewComponentException(
                "ViewContext " + contexts[0].getClass().getSimpleName() +
                " must be an inner class of a @ViewComponent. " +
                "Did you forget to define it as a nested record/class?"
            );
        }

        // Validate all contexts are from the same component
        for (IViewContext ctx : contexts) {
            Class<?> actualComponent = ctx.getClass().getEnclosingClass();

            if (actualComponent != expectedComponent) {
                throw new ViewComponentException(
                    "All ViewContexts in MultiViewContext must be from the same ViewComponent. " +
                    "Expected: " + expectedComponent.getSimpleName() + ", " +
                    "found: " + actualComponent.getSimpleName() + " " +
                    "for ViewContext: " + ctx.getClass().getSimpleName() + ". " +
                    "To compose ViewContexts from different components, use nested components instead."
                );
            }
        }
    }
}
```

**Why same component requirement:**
- Simple template resolution (one template)
- Clear ownership and organization
- Cross-component composition uses existing `view:component` directive

### 2. Template Resolution

**Updated ViewContextMethodReturnValueHandler:**

```kotlin
@Component
class ViewContextMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return IViewContext::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        val viewContext = returnValue as IViewContext

        if (viewContext is MultiViewContext) {
            // Use first context to resolve template (all from same component)
            val firstContext = viewContext.getContexts().first()
            mavContainer.view = IViewContext.getViewComponentTemplateWithoutSuffix(firstContext)

            // Add each context to model with lowercase simple name
            viewContext.getContexts().forEach { ctx ->
                val variableName = ctx.javaClass.simpleName
                    .replaceFirstChar { it.lowercase() }
                mavContainer.addAttribute(variableName, ctx)
            }
        } else {
            // Single context - existing behavior
            mavContainer.view = IViewContext.getViewComponentTemplateWithoutSuffix(viewContext)
            val variableName = viewContext.javaClass.simpleName
                .replaceFirstChar { it.lowercase() }
            mavContainer.addAttribute(variableName, viewContext)
        }
    }
}
```

**Model attributes:**
- `PrimaryButton` → added to model as `primaryButton`
- `Header`, `Content`, `Footer` → added as `header`, `content`, `footer`

### 3. Thymeleaf Integration

**ThymeleafViewContextFragmentProcessor:**

```kotlin
class ThymeleafViewContextFragmentProcessor(
    dialectPrefix: String,
    private val applicationContext: ApplicationContext
) : AbstractAttributeTagProcessor(
    TemplateMode.HTML,
    dialectPrefix,
    null, // Any element
    false,
    "context", // Attribute name: view:context
    true,
    PRECEDENCE,
    true // Remove attribute
) {

    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        attributeName: AttributeName,
        attributeValue: String, // e.g., "Header"
        structureHandler: IElementTagStructureHandler
    ) {
        val webContext = context as WebEngineContext

        // Check if a ViewContext of this type exists in the model
        val variableName = attributeValue.replaceFirstChar { it.lowercase() }
        val hasContext = webContext.getVariable(variableName) != null

        if (!hasContext) {
            // No matching context in model, remove this fragment
            structureHandler.removeElement()
        }
        // Otherwise render normally (attribute is removed automatically)
    }
}
```

**Register in dialect:**

```kotlin
class ThymeleafViewComponentDialect(
    private val applicationContext: ApplicationContext
) : AbstractProcessorDialect(NAME, PREFIX, PRECEDENCE) {

    override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
        return setOf(
            ThymeleafViewComponentTagProcessor(dialectPrefix, applicationContext),
            ThymeleafViewContextFragmentProcessor(dialectPrefix, applicationContext) // NEW
        )
    }

    companion object {
        const val NAME = "ViewComponent Dialect"
        const val PREFIX = "view"
        const val PRECEDENCE = 1000
    }
}
```

### 4. JTE/KTE Integration

JTE already supports type-based conditionals via `instanceof`:

```java
@import de.example.ButtonComponent.*

@if(model instanceof PrimaryButton primaryButton)
    <button class="btn btn-primary" data-action="${primaryButton.action()}">
        ${primaryButton.label()}
    </button>
@elseif(model instanceof SecondaryButton secondaryButton)
    <button class="btn btn-secondary" data-action="${secondaryButton.action()}">
        ${secondaryButton.label()}
    </button>
@elseif(model instanceof DangerButton dangerButton)
    <button class="btn btn-danger"
            data-action="${dangerButton.action()}"
            onclick="return confirm('${dangerButton.confirmMsg()}')">
        ${dangerButton.label()}
    </button>
@endif
```

**For MultiViewContext with JTE:**

```java
@import de.example.PageComponent.*

<%-- Access each context if present --%>
@if(header != null)
    <header>
        <h1>${header.title()}</h1>
    </header>
@endif

@if(content != null)
    <main>
        <p>${content.body()}</p>
    </main>
@endif

@if(footer != null)
    <footer>
        <small>${footer.text()}</small>
    </footer>
@endif
```

**No changes needed to JTE/KTE integrations** - existing features support both patterns.

### 5. Fragment Rendering Rules

**Rule 1: No `view:context` attribute → Always render**

```html
<header>
    <h1>Always visible</h1>
</header>
```

**Rule 2: Has `view:context` attribute → Render only if matching ViewContext in model**

```html
<div view:context="ErrorAlert">
    Only renders if errorAlert is in model
</div>
```

**Rule 3: Multiple fragments can render**

```html
<div view:context="Header">Header</div>
<div view:context="Content">Content</div>
<div view:context="Footer">Footer</div>
```

With `MultiViewContext.of(new Header(...), new Content(...))`:
- Header fragment renders
- Content fragment renders
- Footer fragment removed (not in model)

---

## Examples

### Example 1: Button Variants

```java
@ViewComponent
public class ButtonComponent {

    public record PrimaryButton(String label, String action) implements ViewContext {}
    public record SecondaryButton(String label, String action) implements ViewContext {}
    public record DangerButton(String label, String action, String confirmMsg) implements ViewContext {}

    public PrimaryButton primary(String label, String action) {
        return new PrimaryButton(label, action);
    }

    public SecondaryButton secondary(String label, String action) {
        return new SecondaryButton(label, action);
    }

    public DangerButton danger(String label, String action, String confirmMsg) {
        return new DangerButton(label, action, confirmMsg);
    }
}
```

**Template (Thymeleaf):**
```html
<!-- ButtonComponent.html -->

<button view:context="PrimaryButton" class="btn btn-primary"
        th:attr="data-action=${primaryButton.action}">
    <span th:text="${primaryButton.label}">Primary</span>
</button>

<button view:context="SecondaryButton" class="btn btn-secondary"
        th:attr="data-action=${secondaryButton.action}">
    <span th:text="${secondaryButton.label}">Secondary</span>
</button>

<button view:context="DangerButton" class="btn btn-danger"
        th:attr="data-action=${dangerButton.action}"
        th:onclick="|confirm('${dangerButton.confirmMsg}')|">
    <span th:text="${dangerButton.label}">Danger</span>
</button>
```

**Controller:**
```java
@Controller
public class ButtonController {

    @Autowired
    private ButtonComponent buttonComponent;

    @GetMapping("/button/submit")
    ViewContext submitButton() {
        return buttonComponent.primary("Submit", "/submit");
    }

    @GetMapping("/button/delete")
    ViewContext deleteButton() {
        return buttonComponent.danger("Delete", "/delete", "Are you sure?");
    }
}
```

### Example 2: Page Layout with Optional Footer

```java
@ViewComponent
public class PageComponent {

    public record Header(String title) implements ViewContext {}
    public record Content(String body) implements ViewContext {}
    public record Footer(String text) implements ViewContext {}

    public ViewContext render(String title, String body, boolean includeFooter) {
        return MultiViewContext.of(
            new Header(title),
            new Content(body),
            includeFooter ? new Footer("© 2026") : null
        );
    }
}
```

**Template:**
```html
<!-- PageComponent.html -->

<header view:context="Header">
    <h1 th:text="${header.title}">Title</h1>
</header>

<main view:context="Content">
    <p th:text="${content.body}">Content</p>
</main>

<footer view:context="Footer">
    <small th:text="${footer.text}">Footer</small>
</footer>
```

### Example 3: Alert with Sealed Interface

```java
@ViewComponent
public class AlertComponent {

    sealed interface Alert extends ViewContext {
        String message();
    }

    public record InfoAlert(String message) implements Alert {}
    public record WarningAlert(String message, String details) implements Alert {}
    public record ErrorAlert(String message, String stackTrace) implements Alert {}

    public InfoAlert info(String message) {
        return new InfoAlert(message);
    }

    public WarningAlert warning(String message, String details) {
        return new WarningAlert(message, details);
    }

    public ErrorAlert error(String message, String stackTrace) {
        return new ErrorAlert(message, stackTrace);
    }
}
```

**Template:**
```html
<!--/*@thymesVar id="alert" type="de.example.AlertComponent.Alert"*/-->
<!--/*@thymesVar id="warningAlert" type="de.example.AlertComponent.WarningAlert"*/-->
<!--/*@thymesVar id="errorAlert" type="de.example.AlertComponent.ErrorAlert"*/-->

<div view:context="InfoAlert" class="alert alert-info">
    <i class="icon-info"></i>
    <span th:text="${alert.message}">Info</span>
</div>

<div view:context="WarningAlert" class="alert alert-warning">
    <i class="icon-warning"></i>
    <span th:text="${alert.message}">Warning</span>
    <details>
        <summary>Details</summary>
        <pre th:text="${warningAlert.details}">Details</pre>
    </details>
</div>

<div view:context="ErrorAlert" class="alert alert-error">
    <i class="icon-error"></i>
    <span th:text="${alert.message}">Error</span>
    <details>
        <summary>Stack Trace</summary>
        <pre th:text="${errorAlert.stackTrace}">Stack trace</pre>
    </details>
</div>
```

### Example 4: Dashboard with Conditional Sections

```java
@ViewComponent
public class DashboardComponent {

    public record Stats(int users, int orders) implements ViewContext {}
    public record Chart(List<DataPoint> data) implements ViewContext {}
    public record Notifications(List<String> messages) implements ViewContext {}

    public ViewContext render(User user) {
        var contexts = new ArrayList<ViewContext>();

        // Always show stats
        contexts.add(new Stats(
            userService.count(),
            orderService.count()
        ));

        // Premium users get charts
        if (user.isPremium()) {
            contexts.add(new Chart(analyticsService.getData()));
        }

        // Show notifications if any
        var messages = notificationService.getUnread(user);
        if (!messages.isEmpty()) {
            contexts.add(new Notifications(messages));
        }

        return MultiViewContext.of(contexts.toArray(ViewContext[]::new));
    }
}
```

**Template:**
```html
<!-- DashboardComponent.html -->

<!-- Always renders -->
<div view:context="Stats" class="stats-panel">
    <div class="stat">
        <label>Users</label>
        <span th:text="${stats.users}">0</span>
    </div>
    <div class="stat">
        <label>Orders</label>
        <span th:text="${stats.orders}">0</span>
    </div>
</div>

<!-- Only for premium users -->
<div view:context="Chart" class="chart-panel">
    <canvas id="chart" th:data-points="${chart.data}"></canvas>
</div>

<!-- Only if notifications exist -->
<div view:context="Notifications" class="notifications">
    <h3>Notifications</h3>
    <ul>
        <li th:each="msg : ${notifications.messages}" th:text="${msg}">Message</li>
    </ul>
</div>
```

### Example 5: Layout Variants

```java
@ViewComponent
public class LayoutComponent {

    public record AdminNav(String username) implements ViewContext {}
    public record UserNav(String username) implements ViewContext {}
    public record GuestNav() implements ViewContext {}
    public record Content(ViewContext body) implements ViewContext {}

    public ViewContext adminLayout(String username, ViewContext body) {
        return MultiViewContext.of(
            new AdminNav(username),
            new Content(body)
        );
    }

    public ViewContext userLayout(String username, ViewContext body) {
        return MultiViewContext.of(
            new UserNav(username),
            new Content(body)
        );
    }

    public ViewContext guestLayout(ViewContext body) {
        return MultiViewContext.of(
            new GuestNav(),
            new Content(body)
        );
    }
}
```

**Template:**
```html
<!-- LayoutComponent.html -->
<!DOCTYPE html>
<html>
<head><title>Application</title></head>
<body>

<!-- Only one nav renders based on which is in model -->
<nav view:context="AdminNav" class="admin-nav">
    <span>Admin Panel</span>
    <span th:text="${adminNav.username}">Admin</span>
    <a href="/admin">Dashboard</a>
    <a href="/logout">Logout</a>
</nav>

<nav view:context="UserNav" class="user-nav">
    <span>Welcome, <span th:text="${userNav.username}">User</span></span>
    <a href="/profile">Profile</a>
    <a href="/logout">Logout</a>
</nav>

<nav view:context="GuestNav" class="guest-nav">
    <span>Welcome, Guest</span>
    <a href="/login">Login</a>
    <a href="/register">Register</a>
</nav>

<!-- Always renders -->
<main view:context="Content">
    <div view:component="${content.body}"></div>
</main>

</body>
</html>
```

---

## Implementation Considerations

### 1. thymeVar Comments for IDE Autocomplete

**Pattern for Thymeleaf templates:**

```html
<!--/*@thymesVar id="primaryButton" type="de.example.ButtonComponent.PrimaryButton"*/-->
<!--/*@thymesVar id="secondaryButton" type="de.example.ButtonComponent.SecondaryButton"*/-->
<!--/*@thymesVar id="dangerButton" type="de.example.ButtonComponent.DangerButton"*/-->
```

**With sealed interfaces:**
```html
<!-- Base interface for shared properties -->
<!--/*@thymesVar id="alert" type="de.example.AlertComponent.Alert"*/-->
<!-- Specific types for unique properties -->
<!--/*@thymesVar id="warningAlert" type="de.example.AlertComponent.WarningAlert"*/-->
<!--/*@thymesVar id="errorAlert" type="de.example.AlertComponent.ErrorAlert"*/-->
```

**Best practice:**
- Add thymeVar comment for each ViewContext type
- Use sealed interface variable for shared properties
- Use specific type variable for unique properties

### 2. Validation Strategy

**Runtime validation only** - no template scanning or compile-time validation.

**Why:**
- Runtime validation catches the critical issue (mixed components)
- Template errors are caught during development/testing
- Template engines validate property access
- IDE plugins are better place for template validation

**Validation in MultiViewContext.of():**
- ✅ Validates all ViewContexts from same component
- ✅ Clear error messages
- ✅ Filters out nulls automatically
- ✅ Zero overhead (runs once per request)

### 3. Error Handling

**Clear error messages:**

```java
// Wrong: mixing components
MultiViewContext.of(
    new PageComponent.Header("Title"),
    new FooterComponent.Footer("Footer")  // Different component!
)

// Error message:
"All ViewContexts in MultiViewContext must be from the same ViewComponent.
Expected: PageComponent, found: FooterComponent for ViewContext: Footer.
To compose ViewContexts from different components, use nested components instead."
```

```java
// Wrong: not an inner class
public record Header(String title) implements ViewContext {}  // Top-level!

MultiViewContext.of(new Header("Title"))

// Error message:
"ViewContext Header must be an inner class of a @ViewComponent.
Did you forget to define it as a nested record/class?"
```

### 4. Performance

**Fragment Selection:**
- Thymeleaf: One model lookup per fragment (`webContext.getVariable(variableName)`)
- JTE/KTE: Compiled to native if-else statements (zero overhead)

**MultiViewContext:**
- Validation runs once per MultiViewContext.of() call
- Model population: one addAttribute per context
- Negligible overhead

### 5. Template Organization

**Best practices:**

1. **Group related fragments together:**
   ```html
   <!-- Navigation fragments -->
   <nav view:context="AdminNav">...</nav>
   <nav view:context="UserNav">...</nav>
   <nav view:context="GuestNav">...</nav>

   <!-- Content -->
   <main view:context="Content">...</main>
   ```

2. **Use comments to document fragments:**
   ```html
   <!-- Header: Renders for all page types -->
   <header view:context="Header">...</header>

   <!-- Sidebar: Premium users only -->
   <aside view:context="Sidebar">...</aside>
   ```

3. **Keep always-visible content without view:context:**
   ```html
   <footer>
       <!-- No view:context - always renders -->
       <p>© 2026 Company</p>
   </footer>
   ```

### 6. Testing

**Unit testing fragments:**

```java
@SpringBootTest
class ButtonComponentTest {

    @Autowired
    private ButtonComponent buttonComponent;

    @Test
    void primaryButtonShouldHavePrimaryClass() {
        var button = buttonComponent.primary("Submit", "/submit");

        assertThat(button).isInstanceOf(ButtonComponent.PrimaryButton.class);
        assertThat(button.label()).isEqualTo("Submit");
        assertThat(button.action()).isEqualTo("/submit");
    }

    @Test
    void dangerButtonShouldIncludeConfirmMessage() {
        var button = buttonComponent.danger("Delete", "/delete", "Sure?");

        assertThat(button.confirmMsg()).isEqualTo("Sure?");
    }
}
```

**Integration testing with MockMvc:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pageWithFooterShouldRenderFooter() throws Exception {
        mockMvc.perform(get("/page?footer=true"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("<footer")))
               .andExpect(content().string(containsString("© 2026")));
    }

    @Test
    void pageWithoutFooterShouldNotRenderFooter() throws Exception {
        mockMvc.perform(get("/page?footer=false"))
               .andExpect(status().isOk())
               .andExpect(content().string(not(containsString("<footer"))));
    }
}
```

---

## Migration Path

### Phase 1: Core Infrastructure (v0.10.0)

**Core Module:**
1. Add `MultiViewContext` class with validation
2. Update `IViewContext` companion methods if needed
3. Add fragment-related exceptions

**Thymeleaf Module:**
1. Implement `ThymeleafViewContextFragmentProcessor`
2. Register processor in `ThymeleafViewComponentDialect`
3. Update `ViewContextMethodReturnValueHandler` for MultiViewContext support

**JTE/KTE Modules:**
- No changes needed (already supports instanceof)
- Add documentation and examples

**Testing:**
- Unit tests for MultiViewContext validation
- Integration tests for fragment rendering
- Tests for each template engine

### Phase 2: Documentation & Examples (v0.10.0)

1. Update documentation with fragment rendering guide
2. Add examples to each example project:
   - ButtonComponent (variants)
   - PageComponent (composition)
   - AlertComponent (sealed interfaces)
3. Write migration guide from multiple components to fragments
4. Add thymeVar conventions guide

### Phase 3: Tooling (v0.11.0+)

1. **IntelliJ IDEA Plugin:**
   - Autocomplete for `view:context` attribute values
   - Validation: warn if ViewContext doesn't exist
   - Navigate from `view:context="Header"` to `Header` class
   - Quick-fix: create missing ViewContext

2. **VS Code Extension:**
   - Similar features for VS Code users

3. **CLI Tools:**
   - Code generator for fragment-based components
   - Refactoring tool: convert multiple components to fragments

---

## Alternatives Considered

### Alternative 1: Require view:context-root

**Approach:**
```html
<div view:context-root>
    <div view:context="Header">...</div>
    <div view:context="Content">...</div>
</div>
```

**Decision:** Rejected - unnecessary boilerplate. Template is already scoped to the component.

### Alternative 2: String-Based Fragment Selection

**Approach:**
```java
public record ButtonContext(String variant, ...) implements ViewContext {}
```

```html
<div th:if="${buttonContext.variant == 'primary'}">...</div>
```

**Decision:** Rejected - no type safety, error-prone.

### Alternative 3: Allow Cross-Component ViewContexts in MultiViewContext

**Approach:**
```java
MultiViewContext.of(
    headerComponent.render(),  // HeaderComponent
    contentComponent.render()  // ContentComponent
)
```

**Decision:** Rejected for v0.10 - complex template resolution, unclear ownership. Use nested components for cross-component composition.

### Alternative 4: Compile-Time Validation with Annotation Processor

**Approach:** Annotation processor validates MultiViewContext.of() calls at compile time.

**Decision:** Deferred - complexity not justified. Runtime validation is sufficient.

### Alternative 5: Startup Template Scanning

**Approach:** Scan templates at startup, validate view:context references exist.

**Decision:** Rejected - runtime validation sufficient. Template errors caught during development.

---

## Open Questions

### 1. Should we support wildcard matching?

**Question:** Should `view:context="*"` match any ViewContext (always render)?

**Options:**
- A. No wildcard - use absence of `view:context` for always-render
- B. Support `view:context="*"` for clarity

**Recommendation:** Option A - simpler, no attribute = always render is intuitive.

### 2. Should we support negation?

**Question:** Should `view:context="!Footer"` render when Footer is NOT in model?

**Example:**
```html
<p view:context="!Footer">No footer available</p>
```

**Recommendation:** No - use template engine conditionals (`th:if`) for complex logic.

### 3. Should sealed interfaces be required or optional?

**Question:** Recommend sealed interfaces as best practice or make them optional?

**Recommendation:** Optional but recommended. Document as best practice for shared properties.

### 4. Should we provide a fluent builder for MultiViewContext?

**Question:** Would a builder API improve ergonomics?

```java
return MultiViewContext.builder()
    .with(new Header("Title"))
    .with(new Content("Body"))
    .withIf(showFooter, new Footer("Footer"))
    .build();
```

**Recommendation:** Defer to v0.11 - `of()` with nulls is sufficient for now.

### 5. How to handle empty MultiViewContext?

**Question:** What if all ViewContexts are null?

```java
MultiViewContext.of(
    condition1 ? new Header("Title") : null,
    condition2 ? new Footer("Footer") : null
)
// Both null!
```

**Current behavior:** Throws exception "requires at least one non-null ViewContext"

**Alternative:** Render empty content?

**Recommendation:** Keep exception - forces explicit handling.

---

## Summary

This specification proposes adding **fragment rendering** to Spring View Component via two complementary patterns:

1. **Type-Based Variants** - One ViewContext, one fragment (button variants, alert types)
2. **MultiViewContext Composition** - Multiple ViewContexts, multiple fragments (page layouts, optional sections)

**Key Features:**
- ✅ Framework-provided `MultiViewContext` class (zero boilerplate)
- ✅ Presence-based rendering (`view:context` attribute)
- ✅ Same-component requirement (simple template resolution)
- ✅ Runtime validation with clear error messages
- ✅ Sealed interfaces for shared properties
- ✅ Null filtering for conditional composition
- ✅ Works with all template engines (Thymeleaf, JTE, KTE)
- ✅ Backward compatible

**Benefits:**
- Reduces code duplication (one component instead of many)
- Type-safe (compile-time checks for ViewContext types)
- Clean composition (no `Optional<ViewContext>`)
- Better developer experience (clear intent, less boilerplate)

**Next Steps:**
1. Review and gather feedback
2. Implement Phase 1 (Core Infrastructure)
3. Release as experimental feature in v0.10.0
4. Iterate based on community feedback

---

## References

- **Thymeleaf Fragments**: https://www.thymeleaf.org/doc/articles/layouts.html
- **Spring View Component**: https://github.com/tschuehly/spring-view-component
- **JTE Documentation**: https://jte.gg/
- **Sealed Classes (Java)**: https://openjdk.org/jeps/409

---

**End of Specification**
