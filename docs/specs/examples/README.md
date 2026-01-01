# Fragment Rendering Examples

This directory contains example implementations demonstrating the proposed fragment rendering feature for Spring View Component.

## Overview

Fragment rendering allows a single ViewComponent to have **multiple ViewContext implementations**, with templates that conditionally render different fragments based on the ViewContext type.

## Examples

### 1. ButtonComponent

**File:** `ButtonComponent.java`, `ButtonComponent.html`, `ButtonComponent.jte`

Demonstrates basic fragment rendering with button variants:
- `PrimaryButton` - Main action buttons
- `SecondaryButton` - Alternative action buttons
- `DangerButton` - Destructive action buttons with confirmation

**Key Concepts:**
- Multiple ViewContext records in one component
- Type-safe fragment selection
- Different template syntax for Thymeleaf vs JTE

### 2. AlertComponent

**File:** `AlertComponent.java`, `AlertComponent.html`

Demonstrates advanced fragment rendering with alert messages:
- `InfoAlert` - Informational messages
- `WarningAlert` - Warnings with expandable details
- `ErrorAlert` - Errors with stack traces
- `SuccessAlert` - Success messages

**Key Concepts:**
- Different data requirements per context type
- Compile-time safety (ErrorAlert requires stackTrace)
- Complex template structures with expandable sections

### 3. ExampleController

**File:** `ExampleController.java`

Demonstrates controller integration:
- Different endpoints returning different ViewContext types
- Same ViewComponent rendering different fragments
- Nested component composition

## How It Works

### Thymeleaf Syntax

```html
<div view:context-root>
    <button view:context="PrimaryButton" class="btn btn-primary">
        <span th:text="${primaryButton.label}">Primary</span>
    </button>

    <button view:context="SecondaryButton" class="btn btn-secondary">
        <span th:text="${secondaryButton.label}">Secondary</span>
    </button>
</div>
```

**Behavior:**
1. The `view:context-root` marks the fragment container
2. Each child element with `view:context` is a fragment
3. Only the fragment matching the current ViewContext type is rendered
4. Non-matching fragments are removed from the output

### JTE Syntax

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

**Behavior:**
1. Standard JTE `instanceof` checks with pattern matching
2. Compile-time type safety
3. Native Java control flow

## Benefits

### 1. Reduced Code Duplication

**Before:**
```java
@ViewComponent
public class PrimaryButtonComponent { ... }

@ViewComponent
public class SecondaryButtonComponent { ... }

@ViewComponent
public class DangerButtonComponent { ... }
```

**After:**
```java
@ViewComponent
public class ButtonComponent {
    public record PrimaryButton(...) implements ViewContext {}
    public record SecondaryButton(...) implements ViewContext {}
    public record DangerButton(...) implements ViewContext {}
}
```

### 2. Type Safety

**Before (error-prone):**
```java
public record ButtonContext(String variant, ...) { }

// Easy to make typos
buttonComponent.render("primry", ...); // No compile error!
```

**After (compile-time checked):**
```java
// Typo caught at compile time
buttonComponent.primary(...); // ✓ Type-safe
buttonComponent.primry(...);  // ✗ Compile error
```

### 3. Better IDE Support

- Autocomplete for ViewContext types
- Refactoring support (rename ViewContext → updates templates)
- Type checking in templates (JTE)

### 4. Clearer Intent

```java
// Clear intent: this is a danger button
var deleteButton = buttonComponent.danger(
    "Delete",
    "/delete",
    "Are you sure?"
);

// vs unclear intent with generic context
var deleteButton = buttonComponent.render(
    "danger", // What does "danger" mean?
    "Delete",
    "/delete",
    "Are you sure?"
);
```

## Comparison with Current Approach

| Aspect | Current Approach | Fragment Rendering |
|--------|-----------------|-------------------|
| **Components** | One ViewContext per ViewComponent | Multiple ViewContexts per ViewComponent |
| **Type Safety** | Separate components or string flags | Type-based fragment selection |
| **Templates** | One template per component OR complex conditionals | One template with multiple fragments |
| **Refactoring** | Rename component class | Rename ViewContext record |
| **Testing** | Test each component separately | Test each ViewContext type |
| **Code Volume** | Higher (more component files) | Lower (one component, multiple contexts) |

## Migration Example

### Before (Multiple Components)

```java
// Three separate components
@ViewComponent
public class PrimaryButtonComponent {
    public record Context(String label, String action) implements ViewContext {}

    public Context render(String label, String action) {
        return new Context(label, action);
    }
}

@ViewComponent
public class SecondaryButtonComponent {
    public record Context(String label, String action) implements ViewContext {}

    public Context render(String label, String action) {
        return new Context(label, action);
    }
}

// Controller
@GetMapping("/submit")
ViewContext submitButton() {
    return primaryButtonComponent.render("Submit", "/submit");
}

@GetMapping("/cancel")
ViewContext cancelButton() {
    return secondaryButtonComponent.render("Cancel", "/cancel");
}
```

### After (Fragment Rendering)

```java
// One component with multiple contexts
@ViewComponent
public class ButtonComponent {
    public record PrimaryButton(String label, String action) implements ViewContext {}
    public record SecondaryButton(String label, String action) implements ViewContext {}

    public PrimaryButton primary(String label, String action) {
        return new PrimaryButton(label, action);
    }

    public SecondaryButton secondary(String label, String action) {
        return new SecondaryButton(label, action);
    }
}

// Controller
@GetMapping("/submit")
ViewContext submitButton() {
    return buttonComponent.primary("Submit", "/submit");
}

@GetMapping("/cancel")
ViewContext cancelButton() {
    return buttonComponent.secondary("Cancel", "/cancel");
}
```

## Testing

### Testing Individual Fragments

```java
@SpringBootTest
class ButtonComponentTest {

    @Autowired
    private ButtonComponent buttonComponent;

    @Autowired
    private TemplateRenderer templateRenderer; // Hypothetical renderer

    @Test
    void shouldRenderPrimaryButton() {
        var button = buttonComponent.primary("Submit", "/submit");
        var html = templateRenderer.render(button);

        assertThat(html).contains("btn-primary");
        assertThat(html).contains("Submit");
        assertThat(html).doesNotContain("btn-secondary");
    }

    @Test
    void shouldRenderDangerButtonWithConfirmation() {
        var button = buttonComponent.danger("Delete", "/delete", "Are you sure?");
        var html = templateRenderer.render(button);

        assertThat(html).contains("btn-danger");
        assertThat(html).contains("Delete");
        assertThat(html).contains("confirm('Are you sure?')");
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class ButtonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderPrimaryButtonFragment() throws Exception {
        mockMvc.perform(get("/button/submit"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("btn-primary")))
               .andExpect(content().string(not(containsString("btn-secondary"))));
    }

    @Test
    void shouldRenderDangerButtonFragment() throws Exception {
        mockMvc.perform(get("/button/delete"))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("btn-danger")))
               .andExpect(content().string(containsString("confirm(")));
    }
}
```

## Advanced Patterns

### 1. Nested Fragment Components

```java
@ViewComponent
public class PageComponent {
    public record Page(ViewContext header, ViewContext content, ViewContext footer)
        implements ViewContext {}

    public Page render(ViewContext header, ViewContext content, ViewContext footer) {
        return new Page(header, content, footer);
    }
}

// Usage
pageComponent.render(
    headerComponent.admin("Admin Panel"),
    contentComponent.dashboard(),
    footerComponent.standard()
)
```

### 2. Sealed Interfaces (Java 17+)

```java
@ViewComponent
public class ButtonComponent {

    // Exhaustiveness checking with sealed types
    public sealed interface Button extends ViewContext
        permits PrimaryButton, SecondaryButton, DangerButton {}

    public record PrimaryButton(String label, String action) implements Button {}
    public record SecondaryButton(String label, String action) implements Button {}
    public record DangerButton(String label, String action, String confirm) implements Button {}

    // Compiler ensures all cases are handled
    public Button create(String variant, String label, String action) {
        return switch (variant) {
            case "primary" -> new PrimaryButton(label, action);
            case "secondary" -> new SecondaryButton(label, action);
            case "danger" -> new DangerButton(label, action, "Are you sure?");
            // No default needed - compiler knows all cases are covered
        };
    }
}
```

### 3. Fragment with Shared Logic

```java
@ViewComponent
public class FormFieldComponent {

    // Base interface for shared properties
    sealed interface Field extends ViewContext {
        String name();
        String label();
        String value();
    }

    public record TextField(String name, String label, String value)
        implements Field {}

    public record TextFieldWithError(String name, String label, String value, String error)
        implements Field {}

    public record TextArea(String name, String label, String value, int rows)
        implements Field {}
}
```

## Best Practices

### 1. Keep Fragments Focused

Each fragment should represent a distinct presentation variant, not just minor differences:

**Good:**
```java
public record PrimaryButton(...) implements ViewContext {}
public record DangerButton(...) implements ViewContext {}
```

**Bad (use template conditionals instead):**
```java
public record ButtonWithIcon(...) implements ViewContext {}
public record ButtonWithoutIcon(...) implements ViewContext {}
```

### 2. Use Descriptive Names

ViewContext names become part of the template syntax, so use clear, descriptive names:

**Good:**
```java
public record ErrorAlert(String message, String stackTrace) implements ViewContext {}
```

**Bad:**
```java
public record Alert1(String message, String stackTrace) implements ViewContext {}
```

### 3. Leverage Type Safety

Use different fields for different contexts to enforce correct usage:

**Good:**
```java
public record DangerButton(String label, String action, String confirmMessage)
    implements ViewContext {}
```

**Bad:**
```java
public record Button(String label, String action, String confirmMessage /* nullable */)
    implements ViewContext {}
```

### 4. Document Fragment Purpose

Add Javadoc to explain when each context should be used:

```java
/**
 * Renders a danger button for destructive actions.
 *
 * @param confirmMessage Message shown in confirmation dialog before action
 */
public DangerButton danger(String label, String action, String confirmMessage) {
    return new DangerButton(label, action, confirmMessage);
}
```

## See Also

- [Fragment Rendering Specification](../fragment-rendering-spec.md) - Full technical specification
- [Spring View Component Documentation](https://github.com/tschuehly/spring-view-component)
- [Thymeleaf Fragments](https://www.thymeleaf.org/doc/articles/layouts.html)
- [JTE Documentation](https://jte.gg/)
