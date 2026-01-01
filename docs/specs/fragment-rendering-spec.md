# Fragment Rendering Specification

**Version:** 1.0-DRAFT
**Date:** 2026-01-01
**Author:** Spring View Component Team

## Table of Contents

1. [Overview](#overview)
2. [Problem Statement](#problem-statement)
3. [Goals](#goals)
4. [Non-Goals](#non-goals)
5. [Current State](#current-state)
6. [Proposed Solution](#proposed-solution)
7. [Technical Design](#technical-design)
8. [Examples](#examples)
9. [Implementation Considerations](#implementation-considerations)
10. [Migration Path](#migration-path)
11. [Alternatives Considered](#alternatives-considered)
12. [Open Questions](#open-questions)

---

## Overview

This specification proposes adding **fragment rendering** capabilities to Spring View Component, inspired by Thymeleaf's fragment system. The goal is to enable:

- **Multiple ViewContext implementations** for a single ViewComponent
- **Type-based conditional rendering** within templates
- **Fragment scoping** based on ViewContext type
- **Reduced code duplication** for components with variations

---

## Problem Statement

### Current Limitations

1. **One ViewContext per ViewComponent**: Each ViewComponent currently supports one ViewContext implementation, leading to:
   - Code duplication when creating similar components with slight variations
   - Proliferation of ViewComponent classes for related functionality
   - Difficulty managing component families (e.g., buttons: primary, secondary, danger)

2. **No Template-Level Conditional Rendering**: Conditional logic must be:
   - Implemented in template engine syntax (`th:if`, JTE conditionals)
   - Spread across multiple template files
   - Difficult to type-check at compile time

3. **Verbose Nesting**: Complex layouts require many nested ViewComponent calls:
   ```java
   layoutComponent.render(
     headerComponent.render(),
     contentComponent.render(),
     footerComponent.render()
   )
   ```

### Real-World Use Cases

#### Use Case 1: Button Component with Variants
```java
// Current approach: Separate components or complex conditionals
@ViewComponent
public class PrimaryButtonComponent { ... }

@ViewComponent
public class SecondaryButtonComponent { ... }

@ViewComponent
public class DangerButtonComponent { ... }
```

**Desired:** One `ButtonComponent` with multiple contexts for variants.

#### Use Case 2: Layout with Different Headers
```java
// Current: Multiple layout components or conditional logic in templates
@ViewComponent
public class AdminLayoutComponent { ... }

@ViewComponent
public class UserLayoutComponent { ... }

@ViewComponent
public class GuestLayoutComponent { ... }
```

**Desired:** One `LayoutComponent` with context-based header selection.

#### Use Case 3: Form Fields with Validation States
```java
// Current: Complex template logic
<div th:if="${fieldContext.hasError}" class="field-error">
  <input th:field="*{value}" class="error">
  <span th:text="${fieldContext.errorMessage}"></span>
</div>
<div th:unless="${fieldContext.hasError}" class="field-normal">
  <input th:field="*{value}">
</div>
```

**Desired:** Type-safe fragments for error/normal states.

---

## Goals

1. **Enable Multiple ViewContext Implementations**: Allow a single ViewComponent to return different ViewContext types
2. **Type-Based Fragment Selection**: Render template fragments based on the ViewContext type
3. **Compile-Time Safety**: Leverage Java/Kotlin type system for fragment selection
4. **Template Engine Agnostic**: Support JTE, KTE, and Thymeleaf
5. **Backward Compatibility**: Existing ViewComponents continue to work unchanged
6. **Natural Templates**: Maintain Thymeleaf's "natural templating" philosophy where possible

---

## Non-Goals

1. **Dynamic Fragment Selection**: Runtime string-based fragment selection (use template engine features)
2. **Cross-Component Fragments**: Sharing fragments across different ViewComponents (separate feature)
3. **Fragment Composition**: Nesting fragments within fragments (can be added later)
4. **Fragment Parameters**: Passing parameters to fragments (can be added later)

---

## Current State

### ViewContext Architecture

```kotlin
// Core interface
interface IViewContext {
    companion object {
        fun getViewComponentTemplateWithoutSuffix(context: IViewContext): String
        fun getViewComponentName(viewContext: Class<out IViewContext>): String
    }
}

// Template-specific interfaces
interface ViewContext : IViewContext { } // Thymeleaf
interface ViewContext : Content, IViewContext { } // JTE/KTE
```

### Current Template Resolution

Template path is resolved from the ViewContext's enclosing class:
```kotlin
val componentName = context.javaClass.enclosingClass.simpleName
val componentPackage = context.javaClass.enclosingClass.`package`.name.replace(".", "/")
return "$componentPackage/$componentName"
```

Example: `de.example.ButtonComponent.PrimaryButton` → `de/example/ButtonComponent.html`

### Template Rendering Flow

1. Controller returns `ViewContext` from ViewComponent render method
2. `ViewComponentAspect` intercepts call, sets `ApplicationContext`
3. `ViewContextMethodReturnValueHandler` resolves template path
4. Template engine renders with ViewContext as model attribute

---

## Proposed Solution

### Core Concept

**One ViewComponent → Multiple ViewContext Implementations → Fragment-Based Template**

```java
@ViewComponent
public class ButtonComponent {

    // Multiple ViewContext implementations
    public record PrimaryButton(String label, String action)
        implements ViewContext {}

    public record SecondaryButton(String label, String action)
        implements ViewContext {}

    public record DangerButton(String label, String action, String confirmMessage)
        implements ViewContext {}

    // Render methods return different context types
    public PrimaryButton renderPrimary(String label, String action) {
        return new PrimaryButton(label, action);
    }

    public SecondaryButton renderSecondary(String label, String action) {
        return new SecondaryButton(label, action);
    }

    public DangerButton renderDanger(String label, String action, String confirmMessage) {
        return new DangerButton(label, action, confirmMessage);
    }
}
```

### Template with Fragments

#### Thymeleaf Syntax

```html
<!-- ButtonComponent.html -->

<!-- Fragment for PrimaryButton context -->
<button view:fragment="PrimaryButton"
        view:context-type="de.example.ButtonComponent.PrimaryButton"
        class="btn btn-primary"
        th:attr="data-action=${primaryButton.action}">
    <span th:text="${primaryButton.label}">Primary Action</span>
</button>

<!-- Fragment for SecondaryButton context -->
<button view:fragment="SecondaryButton"
        view:context-type="de.example.ButtonComponent.SecondaryButton"
        class="btn btn-secondary"
        th:attr="data-action=${secondaryButton.action}">
    <span th:text="${secondaryButton.label}">Secondary Action</span>
</button>

<!-- Fragment for DangerButton context -->
<button view:fragment="DangerButton"
        view:context-type="de.example.ButtonComponent.DangerButton"
        class="btn btn-danger"
        th:attr="data-action=${dangerButton.action}"
        onclick="return confirm('${dangerButton.confirmMessage}')">
    <span th:text="${dangerButton.label}">Danger Action</span>
</button>
```

#### JTE Syntax

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
            onclick="return confirm('${dangerButton.confirmMessage()}')">
        ${dangerButton.label()}
    </button>
@endif
```

#### Alternative Thymeleaf Syntax (Simpler)

```html
<!-- ButtonComponent.html -->
<div view:context-root>

    <!-- Rendered when ViewContext is PrimaryButton -->
    <button view:context="PrimaryButton"
            class="btn btn-primary"
            th:attr="data-action=${primaryButton.action}">
        <span th:text="${primaryButton.label}">Primary Action</span>
    </button>

    <!-- Rendered when ViewContext is SecondaryButton -->
    <button view:context="SecondaryButton"
            class="btn btn-secondary"
            th:attr="data-action=${secondaryButton.action}">
        <span th:text="${secondaryButton.label}">Secondary Action</span>
    </button>

    <!-- Rendered when ViewContext is DangerButton -->
    <button view:context="DangerButton"
            class="btn btn-danger"
            th:attr="data-action=${dangerButton.action}"
            onclick="return confirm('${dangerButton.confirmMessage}')">
        <span th:text="${dangerButton.label}">Danger Action</span>
    </button>

</div>
```

---

## Technical Design

### 1. ViewContext Type Resolution

Extend `IViewContext` to support fragment/context type resolution:

```kotlin
interface IViewContext {
    companion object {
        // Existing methods
        fun getViewComponentTemplateWithoutSuffix(context: IViewContext): String
        fun getViewComponentName(viewContext: Class<out IViewContext>): String

        // NEW: Get ViewContext simple name for fragment matching
        fun getViewContextSimpleName(context: IViewContext): String {
            return context.javaClass.simpleName
        }

        // NEW: Get fully qualified ViewContext name
        fun getViewContextTypeName(context: IViewContext): String {
            return context.javaClass.canonicalName
        }
    }
}
```

### 2. Thymeleaf Integration

#### Option A: Custom Attribute Processor (`view:context`)

```kotlin
class ThymeleafViewContextFragmentProcessor(
    dialectPrefix: String,
    private val applicationContext: ApplicationContext
) : AbstractAttributeTagProcessor(
    TemplateMode.HTML,
    dialectPrefix,
    null, // Any element
    false,
    "context", // Attribute name
    true,
    PRECEDENCE,
    true // Remove attribute
) {

    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        attributeName: AttributeName,
        attributeValue: String, // e.g., "PrimaryButton"
        structureHandler: IElementTagStructureHandler
    ) {
        val webContext = context as WebEngineContext

        // Find the ViewContext in the model
        val viewContext = findViewContextInModel(webContext)
            ?: throw ViewComponentException("No ViewContext found in model")

        // Get the ViewContext simple name
        val contextTypeName = IViewContext.getViewContextSimpleName(viewContext)

        // Match fragment
        if (contextTypeName != attributeValue) {
            // Don't render this fragment
            structureHandler.removeElement()
        }
        // Otherwise, render normally (just remove the view:context attribute)
    }

    private fun findViewContextInModel(context: WebEngineContext): IViewContext? {
        // Search model for IViewContext implementation
        for (variableName in context.variableNames) {
            val value = context.getVariable(variableName)
            if (value is IViewContext) {
                return value
            }
        }
        return null
    }
}
```

#### Option B: Custom Dialect with `view:context-root`

```kotlin
class ThymeleafViewContextRootProcessor(
    dialectPrefix: String,
    private val applicationContext: ApplicationContext
) : AbstractAttributeTagProcessor(
    TemplateMode.HTML,
    dialectPrefix,
    null,
    false,
    "context-root",
    true,
    PRECEDENCE,
    true
) {

    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        attributeName: AttributeName,
        attributeValue: String,
        structureHandler: IElementTagStructureHandler
    ) {
        val webContext = context as WebEngineContext
        val viewContext = findViewContextInModel(webContext)
            ?: throw ViewComponentException("No ViewContext found in model")

        val contextTypeName = IViewContext.getViewContextSimpleName(viewContext)

        // Process children and remove non-matching fragments
        // This requires more complex processing of child elements
        structureHandler.setLocalVariable("_viewContextType", contextTypeName)
    }
}
```

### 3. JTE/KTE Integration

JTE/KTE already support type-based conditionals via `instanceof`:

```java
@import de.example.ButtonComponent.*

@if(model instanceof PrimaryButton primaryButton)
    <button class="btn btn-primary">
        ${primaryButton.label()}
    </button>
@elseif(model instanceof SecondaryButton secondaryButton)
    <button class="btn btn-secondary">
        ${secondaryButton.label()}
    </button>
@endif
```

**Enhancement:** Provide utility templates or macros for fragment selection:

```java
@import static de.example.utils.ViewContextFragments.*

${fragment(model,
    PrimaryButton.class, () -> renderPrimary(model),
    SecondaryButton.class, () -> renderSecondary(model),
    DangerButton.class, () -> renderDanger(model)
)}
```

### 4. Template Resolution Changes

**Current:** Template path based on enclosing class name
**Proposed:** Same behavior (backward compatible)

Template fragments are selected **within** the resolved template, not via different template files.

### 5. Model Attribute Naming

**Current:** ViewContext added to model with auto-generated variable name
**Proposed:** Support both:
- Auto-generated name (backward compatible)
- Simple name based on ViewContext type (e.g., `PrimaryButton` → `primaryButton`)

```kotlin
override fun handleReturnValue(
    returnValue: Any?,
    returnType: MethodParameter,
    mavContainer: ModelAndViewContainer,
    webRequest: NativeWebRequest
) {
    val viewContext = returnValue as IViewContext

    // Resolve template path (unchanged)
    mavContainer.view = IViewContext.getViewComponentTemplateWithoutSuffix(viewContext)

    // Add with auto-generated name (backward compatible)
    mavContainer.addAttribute(viewContext)

    // NEW: Also add with ViewContext simple name
    val contextSimpleName = IViewContext.getViewContextSimpleName(viewContext)
    val variableName = contextSimpleName.replaceFirstChar { it.lowercase() }
    mavContainer.addAttribute(variableName, viewContext)
}
```

---

## Examples

### Example 1: Alert Component

#### ViewComponent

```java
@ViewComponent
public class AlertComponent {

    public record InfoAlert(String message) implements ViewContext {}
    public record WarningAlert(String message, String details) implements ViewContext {}
    public record ErrorAlert(String message, String stackTrace) implements ViewContext {}
    public record SuccessAlert(String message) implements ViewContext {}

    public InfoAlert info(String message) {
        return new InfoAlert(message);
    }

    public WarningAlert warning(String message, String details) {
        return new WarningAlert(message, details);
    }

    public ErrorAlert error(String message, String stackTrace) {
        return new ErrorAlert(message, stackTrace);
    }

    public SuccessAlert success(String message) {
        return new SuccessAlert(message);
    }
}
```

#### Thymeleaf Template (`AlertComponent.html`)

```html
<div view:context-root>

    <div view:context="InfoAlert" class="alert alert-info">
        <i class="icon-info"></i>
        <span th:text="${infoAlert.message}">Info message</span>
    </div>

    <div view:context="WarningAlert" class="alert alert-warning">
        <i class="icon-warning"></i>
        <span th:text="${warningAlert.message}">Warning message</span>
        <details>
            <summary>Details</summary>
            <pre th:text="${warningAlert.details}">Warning details</pre>
        </details>
    </div>

    <div view:context="ErrorAlert" class="alert alert-error">
        <i class="icon-error"></i>
        <span th:text="${errorAlert.message}">Error message</span>
        <details>
            <summary>Stack Trace</summary>
            <pre th:text="${errorAlert.stackTrace}">Stack trace</pre>
        </details>
    </div>

    <div view:context="SuccessAlert" class="alert alert-success">
        <i class="icon-success"></i>
        <span th:text="${successAlert.message}">Success message</span>
    </div>

</div>
```

#### JTE Template (`AlertComponent.jte`)

```java
@import de.example.AlertComponent.*

@if(model instanceof InfoAlert infoAlert)
    <div class="alert alert-info">
        <i class="icon-info"></i>
        <span>${infoAlert.message()}</span>
    </div>
@elseif(model instanceof WarningAlert warningAlert)
    <div class="alert alert-warning">
        <i class="icon-warning"></i>
        <span>${warningAlert.message()}</span>
        <details>
            <summary>Details</summary>
            <pre>${warningAlert.details()}</pre>
        </details>
    </div>
@elseif(model instanceof ErrorAlert errorAlert)
    <div class="alert alert-error">
        <i class="icon-error"></i>
        <span>${errorAlert.message()}</span>
        <details>
            <summary>Stack Trace</summary>
            <pre>${errorAlert.stackTrace()}</pre>
        </details>
    </div>
@elseif(model instanceof SuccessAlert successAlert)
    <div class="alert alert-success">
        <i class="icon-success"></i>
        <span>${successAlert.message()}</span>
    </div>
@endif
```

#### Controller Usage

```java
@Controller
public class NotificationController {

    @Autowired
    private AlertComponent alertComponent;

    @GetMapping("/success")
    ViewContext showSuccess() {
        return alertComponent.success("Operation completed successfully!");
    }

    @GetMapping("/error")
    ViewContext showError() {
        return alertComponent.error(
            "An error occurred",
            "java.lang.RuntimeException: Database connection failed..."
        );
    }
}
```

### Example 2: Form Field Component

#### ViewComponent

```java
@ViewComponent
public class FormFieldComponent {

    public record TextField(String name, String label, String value)
        implements ViewContext {}

    public record TextFieldWithError(String name, String label, String value, String error)
        implements ViewContext {}

    public record TextArea(String name, String label, String value, int rows)
        implements ViewContext {}

    public record Select(String name, String label, String value, List<Option> options)
        implements ViewContext {
        public record Option(String value, String label) {}
    }

    public TextField textField(String name, String label, String value) {
        return new TextField(name, label, value);
    }

    public TextFieldWithError textFieldWithError(String name, String label, String value, String error) {
        return new TextFieldWithError(name, label, value, error);
    }

    public TextArea textArea(String name, String label, String value, int rows) {
        return new TextArea(name, label, value, rows);
    }

    public Select select(String name, String label, String value, List<Select.Option> options) {
        return new Select(name, label, value, options);
    }
}
```

#### Thymeleaf Template (`FormFieldComponent.html`)

```html
<div view:context-root>

    <div view:context="TextField" class="form-field">
        <label th:for="${textField.name}" th:text="${textField.label}">Label</label>
        <input type="text"
               th:id="${textField.name}"
               th:name="${textField.name}"
               th:value="${textField.value}">
    </div>

    <div view:context="TextFieldWithError" class="form-field form-field-error">
        <label th:for="${textFieldWithError.name}" th:text="${textFieldWithError.label}">Label</label>
        <input type="text"
               class="error"
               th:id="${textFieldWithError.name}"
               th:name="${textFieldWithError.name}"
               th:value="${textFieldWithError.value}">
        <span class="error-message" th:text="${textFieldWithError.error}">Error message</span>
    </div>

    <div view:context="TextArea" class="form-field">
        <label th:for="${textArea.name}" th:text="${textArea.label}">Label</label>
        <textarea th:id="${textArea.name}"
                  th:name="${textArea.name}"
                  th:rows="${textArea.rows}"
                  th:text="${textArea.value}">Value</textarea>
    </div>

    <div view:context="Select" class="form-field">
        <label th:for="${select.name}" th:text="${select.label}">Label</label>
        <select th:id="${select.name}" th:name="${select.name}">
            <option th:each="option : ${select.options}"
                    th:value="${option.value}"
                    th:text="${option.label}"
                    th:selected="${option.value == select.value}">Option</option>
        </select>
    </div>

</div>
```

### Example 3: Layout with Different Headers

#### ViewComponent

```java
@ViewComponent
public class LayoutComponent {

    public record AdminLayout(ViewContext content, String adminName)
        implements ViewContext {}

    public record UserLayout(ViewContext content, String username)
        implements ViewContext {}

    public record GuestLayout(ViewContext content)
        implements ViewContext {}

    public AdminLayout adminLayout(ViewContext content, String adminName) {
        return new AdminLayout(content, adminName);
    }

    public UserLayout userLayout(ViewContext content, String username) {
        return new UserLayout(content, username);
    }

    public GuestLayout guestLayout(ViewContext content) {
        return new GuestLayout(content);
    }
}
```

#### Thymeleaf Template (`LayoutComponent.html`)

```html
<!DOCTYPE html>
<html>
<head>
    <title>Application</title>
</head>
<body>

<div view:context-root>

    <!-- Admin Layout -->
    <div view:context="AdminLayout">
        <nav class="navbar navbar-admin">
            <span>Admin Panel</span>
            <span th:text="${adminLayout.adminName}">Admin Name</span>
            <a href="/admin/logout">Logout</a>
        </nav>
        <main>
            <div view:component="${adminLayout.content}"></div>
        </main>
        <footer>Admin Footer</footer>
    </div>

    <!-- User Layout -->
    <div view:context="UserLayout">
        <nav class="navbar navbar-user">
            <span>Welcome</span>
            <span th:text="${userLayout.username}">Username</span>
            <a href="/logout">Logout</a>
        </nav>
        <main>
            <div view:component="${userLayout.content}"></div>
        </main>
        <footer>User Footer</footer>
    </div>

    <!-- Guest Layout -->
    <div view:context="GuestLayout">
        <nav class="navbar navbar-guest">
            <span>Guest Access</span>
            <a href="/login">Login</a>
        </nav>
        <main>
            <div view:component="${guestLayout.content}"></div>
        </main>
        <footer>Guest Footer</footer>
    </div>

</div>

</body>
</html>
```

---

## Implementation Considerations

### 1. Performance

**Fragment Selection Overhead:**
- Thymeleaf: Minimal - one attribute check per fragment element
- JTE/KTE: Zero overhead - compiled to native Java if-else

**Model Attribute Duplication:**
- Adding ViewContext with two names (auto + simple name) has negligible memory impact

### 2. Error Handling

**No Matching Fragment:**
```kotlin
class FragmentNotFoundException(
    val viewContext: IViewContext,
    val availableFragments: List<String>
) : ViewComponentException(
    "No fragment found for ViewContext type '${viewContext.javaClass.simpleName}'. " +
    "Available fragments: ${availableFragments.joinToString(", ")}"
)
```

**Multiple Matching Fragments:**
```kotlin
class MultipleFragmentsException(
    val viewContext: IViewContext,
    val fragmentName: String
) : ViewComponentException(
    "Multiple fragments found for context type '$fragmentName'. " +
    "Only one fragment per ViewContext type is allowed."
)
```

### 3. Development Experience

**IDE Support:**
- ViewContext type names in `view:context` are strings (no autocomplete)
- Consider IntelliJ/Eclipse plugin for validation

**Hot Reload:**
- Fragment changes should trigger template recompilation
- No changes needed to existing hot-reload mechanism

**Debugging:**
- Add logging for fragment selection
- Template comments indicating which fragment was selected

### 4. Testing

**Unit Tests:**
```java
@Test
void shouldRenderPrimaryButtonFragment() {
    var button = buttonComponent.renderPrimary("Submit", "/submit");

    var html = renderToString(button);

    assertThat(html).contains("btn-primary");
    assertThat(html).contains("Submit");
    assertThat(html).doesNotContain("btn-secondary");
}
```

**Integration Tests:**
```java
@Test
void shouldSelectCorrectFragmentBasedOnContext() {
    mockMvc.perform(get("/button/primary"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("btn-primary")));

    mockMvc.perform(get("/button/danger"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("btn-danger")));
}
```

### 5. Documentation

**Required Documentation:**
1. Fragment rendering guide
2. Migration guide from multiple components to fragments
3. Template syntax reference for each engine
4. Best practices and patterns
5. Troubleshooting guide

### 6. Backward Compatibility

**Existing Code:**
- All existing ViewComponents work unchanged
- Fragment syntax is opt-in
- No breaking changes to API

**Migration:**
- Developers can gradually refactor to fragments
- Provide codemods/refactoring tools

---

## Migration Path

### Phase 1: Core Infrastructure (v0.10.0)

**Core Module:**
1. Add fragment resolution methods to `IViewContext`
2. Update `ViewContextMethodReturnValueHandler` to add ViewContext with simple name
3. Add fragment-related exceptions

**Thymeleaf Module:**
1. Implement `ThymeleafViewContextFragmentProcessor`
2. Register processor in `ThymeleafViewComponentDialect`
3. Add configuration properties for fragment behavior

**JTE/KTE Module:**
1. No changes needed (already supports `instanceof`)
2. Add utility classes/macros for fragment selection

**Testing:**
- Unit tests for fragment selection logic
- Integration tests for each template engine
- Performance benchmarks

### Phase 2: Documentation & Examples (v0.10.0)

1. Add fragment examples to each example project
2. Write comprehensive documentation
3. Create migration guide
4. Record tutorial videos

### Phase 3: Tooling & Developer Experience (v0.11.0)

1. IntelliJ IDEA plugin for fragment validation
2. Template linting rules
3. Code generation templates
4. Refactoring tools

### Phase 4: Advanced Features (Future)

1. Fragment parameters
2. Fragment composition
3. Cross-component fragment sharing
4. Fragment testing utilities

---

## Alternatives Considered

### Alternative 1: String-Based Fragment Selection (Thymeleaf-style)

**Approach:**
```java
public ViewContext render(String variant) {
    return new ButtonContext(variant, "Submit", "/submit");
}
```

```html
<div th:fragment="primary">...</div>
<div th:fragment="secondary">...</div>
```

**Pros:**
- Familiar to Thymeleaf users
- Flexible (runtime fragment selection)

**Cons:**
- No compile-time safety
- String-based matching prone to typos
- Difficult to refactor
- No IDE autocomplete

**Decision:** Rejected in favor of type-based approach

### Alternative 2: Separate Template Files per Fragment

**Approach:**
```
ButtonComponent-Primary.html
ButtonComponent-Secondary.html
ButtonComponent-Danger.html
```

**Pros:**
- Clear separation
- Easier to navigate

**Cons:**
- File proliferation
- Harder to see related fragments together
- Template path resolution complexity

**Decision:** Rejected - fragments belong together

### Alternative 3: Builder Pattern with Conditional Methods

**Approach:**
```java
public record ButtonContext(
    String variant,
    String label,
    String action,
    String confirmMessage
) implements ViewContext {}

public ButtonContext primary(String label) {
    return new ButtonContext("primary", label, null, null);
}
```

```html
<button th:class="${buttonContext.variant == 'primary' ? 'btn-primary' : 'btn-secondary'}">
  ...
</button>
```

**Pros:**
- Single ViewContext
- Simple implementation

**Cons:**
- Template logic complexity
- No type safety
- Nullable fields

**Decision:** Rejected - defeats purpose of type-based design

### Alternative 4: Sealed Classes (Java 17+)

**Approach:**
```java
public sealed interface ButtonContext permits PrimaryButton, SecondaryButton, DangerButton {
    record PrimaryButton(...) implements ButtonContext {}
    record SecondaryButton(...) implements ButtonContext {}
    record DangerButton(...) implements ButtonContext {}
}
```

**Pros:**
- Exhaustiveness checking
- Modern Java feature
- Clear intent

**Cons:**
- Requires Java 17+
- Current users may be on Java 11/17
- Not compatible with Kotlin sealed classes across languages

**Decision:** Consider for future enhancement (optional feature)

---

## Open Questions

### 1. Fragment Naming Convention

**Question:** Should fragments use simple names or fully qualified names?

**Options:**
- Simple: `view:context="PrimaryButton"`
- Qualified: `view:context="de.example.ButtonComponent.PrimaryButton"`

**Recommendation:** Simple names (less verbose, ViewContext is always in same file)

### 2. Default Fragment

**Question:** Should we support a default fragment when no match is found?

**Options:**
- A. Throw exception (fail fast)
- B. Render nothing (silent failure)
- C. Support `view:context="*"` as fallback

**Recommendation:** Option A initially, add Option C in future release

### 3. Fragment Inheritance

**Question:** Should fragments support inheritance (e.g., base fragment + variant-specific overrides)?

**Example:**
```html
<div view:context-base>
    <button class="btn">
        <span view:context-slot="label"></span>
    </button>
</div>

<div view:context="PrimaryButton">
    <span view:context-slot="label">Primary</span>
</div>
```

**Recommendation:** Defer to future release (significant complexity)

### 4. Fragment Testing

**Question:** Should we provide utilities for testing individual fragments?

**Example:**
```java
@Test
void testPrimaryButtonFragment() {
    var html = fragmentRenderer.render(
        ButtonComponent.class,
        "PrimaryButton",
        new PrimaryButton("Submit", "/submit")
    );

    assertThat(html).contains("btn-primary");
}
```

**Recommendation:** Yes, add in Phase 3

### 5. Multi-Fragment Selection

**Question:** Should one ViewContext be able to match multiple fragments?

**Use Case:** Render both header and footer fragments from same context

**Recommendation:** No - use nested ViewContexts for this pattern

### 6. Fragment Validation

**Question:** Should we validate at startup that all ViewContext types have matching fragments?

**Pros:**
- Early error detection
- Better developer experience

**Cons:**
- Startup time impact
- Complex for conditional fragments

**Recommendation:** Add as opt-in feature via configuration property

---

## Summary

This specification proposes adding **type-based fragment rendering** to Spring View Component, enabling:

1. **Multiple ViewContext implementations per ViewComponent**
2. **Type-safe fragment selection via `view:context` attribute**
3. **Reduced code duplication for component variants**
4. **Improved developer experience with compile-time checks**

**Key Benefits:**
- ✅ Backward compatible
- ✅ Works with all template engines (Thymeleaf, JTE, KTE)
- ✅ Type-safe (leverages Java/Kotlin type system)
- ✅ Minimal performance overhead
- ✅ Natural template support (Thymeleaf)

**Next Steps:**
1. Review and refine this specification
2. Create proof-of-concept implementation
3. Gather community feedback
4. Implement Phase 1 (Core Infrastructure)
5. Release as experimental feature in v0.10.0

---

## References

- **Thymeleaf Fragments Documentation**: https://www.thymeleaf.org/doc/articles/layouts.html
- **Thymeleaf Fragment Tutorial**: https://www.baeldung.com/spring-thymeleaf-fragments
- **Spring View Component Repository**: https://github.com/tschuehly/spring-view-component
- **JTE Documentation**: https://jte.gg/
- **Sealed Classes (Java)**: https://openjdk.org/jeps/409

---

**End of Specification**
