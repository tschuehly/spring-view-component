# Architecture Decision Record: Reactive Features for Spring ViewComponent

**Status:** Proposed
**Date:** 2026-01-15
**Deciders:** Spring ViewComponent Team
**Context:** Adding Laravel Livewire-inspired reactive features to Spring ViewComponent

---

## Decision 1: Use HTMX as Primary Transport Mechanism

### Context
Need to choose a client-server communication mechanism for reactive updates without full page reloads.

### Options Considered

1. **HTMX** - Hypermedia-driven approach
2. **WebSocket** - Full-duplex communication
3. **Server-Sent Events (SSE)** - Server-push only
4. **REST API + React/Vue** - SPA approach

### Decision
**Selected: HTMX (Option 1)**

### Rationale

**Pros:**
- Aligns with Spring ViewComponent's server-rendering philosophy
- Minimal JavaScript footprint (~14KB)
- Progressive enhancement (works without JS)
- Simple mental model for Spring developers
- Excellent SEO (server-rendered by default)
- Easy debugging (standard HTTP requests)
- No need for complex build tooling

**Cons:**
- Higher latency than WebSocket for frequent updates
- Not ideal for real-time collaborative features
- Network overhead for each interaction

**Why Not WebSocket:**
- Significant infrastructure complexity (connection management, load balancing)
- Higher memory usage (persistent connections)
- Harder to debug
- Overkill for majority of use cases

**Why Not SSE:**
- One-way communication (still need HTTP for actions)
- Browser connection limits
- Not universally supported

**Why Not SPA:**
- Contradicts Spring ViewComponent's philosophy
- Loss of server-side rendering benefits
- Increased complexity (separate frontend build)

### Consequences
- Simple implementation path
- Easy adoption for Spring developers
- May need to add WebSocket support later for real-time features
- HTMX learning curve for developers unfamiliar with it

---

## Decision 2: Annotation-Based Component API

### Context
How should developers define reactive components and their properties/actions?

### Options Considered

1. **Annotations** (`@Property`, `@Action`)
2. **Interfaces** (`implements ReactiveComponent`)
3. **XML Configuration**
4. **Builder Pattern** (`component.property("name").action("submit")`)

### Decision
**Selected: Annotations (Option 1)**

### Rationale

**Pros:**
- Familiar to Spring developers
- Declarative and concise
- IDE support (auto-completion, validation)
- Compile-time checking
- Framework can introspect at runtime

**Cons:**
- Annotation processing overhead
- Can't be configured dynamically at runtime

**Why Not Interfaces:**
- More verbose
- Requires implementing methods
- Less flexible

**Why Not XML:**
- Outdated approach
- Verbose
- No type safety
- Poor IDE support

**Why Not Builder:**
- Too verbose
- Imperative rather than declarative
- Error-prone

### Example

```java
@ReactiveViewComponent
public class FormComponent {

    @Property(reactive = true, debounce = 300)
    private String username;

    @Action
    public void submit() {
        // Handle submission
    }
}
```

### Consequences
- Clean, readable component definitions
- Excellent IDE support
- Introspection via reflection
- Consistent with Spring's annotation-driven approach

---

## Decision 3: Session-Based State Storage (Default)

### Context
Where to store component state between requests?

### Options Considered

1. **HTTP Session** - Spring's built-in session
2. **Redis** - External cache
3. **Database** - Persistent storage
4. **Client-Side** - Store state in hidden form fields/localStorage

### Decision
**Selected: HTTP Session (Option 1) with Redis as optional upgrade**

### Rationale

**Pros (Session):**
- Zero configuration
- Works out of the box with Spring Boot
- Familiar to Spring developers
- Can be backed by Redis via Spring Session
- Simple scaling with sticky sessions

**Cons (Session):**
- Memory usage on server
- Requires sticky sessions or shared storage for scaling
- Lost on server restart (unless persisted)

**Why Not Redis by Default:**
- Requires external infrastructure
- Overkill for single-instance applications
- Can be added as opt-in enhancement

**Why Not Database:**
- Slower than in-memory storage
- Requires schema management
- Overkill for transient component state

**Why Not Client-Side:**
- Security concerns (state exposed to client)
- Payload size (sent with every request)
- Validation overhead

### Configuration

```yaml
spring:
  view-component:
    reactive:
      state-storage: session  # Default

# For production with multiple instances:
spring:
  session:
    store-type: redis
  view-component:
    reactive:
      state-storage: redis
```

### Consequences
- Simple default setup
- Clear upgrade path to distributed storage
- Familiar to Spring developers
- May need sticky sessions without Redis

---

## Decision 4: Jackson for Serialization

### Context
How to serialize component state for storage and transmission?

### Options Considered

1. **Jackson (JSON)**
2. **Java Serialization**
3. **Kryo**
4. **Protocol Buffers**

### Decision
**Selected: Jackson (Option 1) with Kryo as optional optimization**

### Rationale

**Pros (Jackson):**
- Already included in Spring Boot
- Human-readable (JSON)
- Works with Java Records natively (Java 16+)
- Excellent debugging experience
- Version tolerant (can ignore unknown fields)
- Good performance

**Cons (Jackson):**
- Larger payload than binary formats
- Slower than Kryo for complex objects

**Why Not Java Serialization:**
- Security vulnerabilities
- Version compatibility issues
- Not human-readable
- Discouraged by modern practices

**Why Not Kryo:**
- Additional dependency
- Configuration required
- Not human-readable
- Can be added as opt-in for performance-critical apps

**Why Not Protocol Buffers:**
- Requires schema definition
- Compilation step
- Overkill for this use case

### Implementation

```java
@Configuration
public class StateSerializationConfig {

    @Bean
    public ComponentStateSerializer stateSerializer(ObjectMapper objectMapper) {
        return new JacksonComponentStateSerializer(objectMapper);
    }

    // Optional: Enable Kryo for performance
    @Bean
    @ConditionalOnProperty("spring.view-component.reactive.use-kryo")
    public ComponentStateSerializer kryoSerializer() {
        return new KryoComponentStateSerializer();
    }
}
```

### Consequences
- Easy debugging (can inspect JSON state)
- Good performance for most use cases
- Works with Records
- Option to optimize with Kryo later

---

## Decision 5: Islands via Targeted Rendering

### Context
How to implement independent component regions that update separately?

### Options Considered

1. **Server-Side Islands** - Annotate islands, server returns only changed regions
2. **Client-Side Virtual DOM** - Diff on client like React
3. **Hybrid** - Server marks changed islands, client applies updates

### Decision
**Selected: Server-Side Islands (Option 1)**

### Rationale

**Pros:**
- Server controls rendering logic
- Minimal JavaScript required
- Type-safe island definitions
- Simple mental model
- Leverages existing template engines

**Cons:**
- Requires careful island boundary definition
- Server must track which islands changed

**Why Not Client-Side Virtual DOM:**
- Requires shipping template logic to client
- Complex diffing algorithm
- Contradicts server-rendering approach

**Why Not Hybrid:**
- More complex implementation
- Unclear performance benefits

### Implementation

```java
@Action
@UpdateIsland("notifications")
public void markRead(Long id) {
    // Only re-renders the "notifications" island
}
```

```html
<div reactive:island="notifications">
    <!-- Only this section updates -->
</div>
```

### Consequences
- Clear, declarative island boundaries
- Efficient partial updates
- Server-controlled rendering
- May need to manually define island dependencies

---

## Decision 6: Alpine.js for Complex Client-Side Logic

### Context
What to use for client-side interactivity beyond simple HTMX actions?

### Options Considered

1. **Alpine.js** - Lightweight reactive framework
2. **Vanilla JavaScript** - No framework
3. **jQuery** - Classic library
4. **Vue.js** - Full framework

### Decision
**Selected: Alpine.js (Option 1) as optional enhancement**

### Rationale

**Pros (Alpine.js):**
- Minimal footprint (~15KB)
- Declarative syntax similar to Vue
- Works perfectly with HTMX
- Good for progressive enhancement
- Large community

**Cons (Alpine.js):**
- Another framework to learn
- Not as powerful as Vue/React

**Why Not Vanilla JS:**
- More verbose for common patterns
- No reactive data binding
- Harder to maintain

**Why Not jQuery:**
- Outdated approach
- Not reactive
- Larger than Alpine

**Why Not Vue:**
- Too heavy for our use case
- Would encourage SPA patterns

### Integration

```html
<div reactive:component="chartComponent"
     x-data="chartComponent()">

    <canvas x-ref="chartCanvas"></canvas>

    <button @click="updateChart()">Update</button>
</div>

<script>
function chartComponent() {
    return {
        chart: null,
        init() {
            this.chart = new Chart(this.$refs.chartCanvas, {
                // Chart configuration
            });

            // Listen for server updates
            this.$reactive.on('data-updated', (data) => {
                this.chart.data = data;
                this.chart.update();
            });
        },
        updateChart() {
            this.$reactive.call('refreshData');
        }
    }
}
</script>
```

### Consequences
- Best of both worlds: server rendering + client interactivity
- Gentle learning curve
- Optional (components work without it)
- May lead to blurred responsibilities (server vs client logic)

---

## Decision 7: Validation via Jakarta Bean Validation + Custom Handlers

### Context
How to handle form validation in reactive components?

### Options Considered

1. **Jakarta Bean Validation** only
2. **Custom validation handlers** only
3. **Hybrid** - Jakarta + Custom handlers

### Decision
**Selected: Hybrid (Option 3)**

### Rationale

**Pros (Hybrid):**
- Jakarta Bean Validation for standard rules (`@NotBlank`, `@Email`, etc.)
- Custom handlers for complex business logic
- Declarative and flexible
- Reuses existing validation infrastructure

**Example:**

```java
@Property(reactive = true)
@NotBlank
@Email
private String email;

@ValidationHandler(property = "email", mode = ValidationMode.LAZY)
public ValidationResult customEmailValidation(String email) {
    if (userRepository.existsByEmail(email)) {
        return ValidationResult.error("Email already registered");
    }
    return ValidationResult.ok();
}
```

### Consequences
- Familiar to Spring developers
- Flexible for complex scenarios
- Can reuse existing validators
- Need to coordinate between Jakarta and custom validators

---

## Decision 8: Multi-Template Engine Support

### Context
Should reactive features work with all supported template engines (Thymeleaf, JTE, KTE)?

### Options Considered

1. **Thymeleaf only** - Start with one engine
2. **All engines from day one**
3. **Engine-agnostic core + adapters**

### Decision
**Selected: Engine-agnostic core + adapters (Option 3)**

### Rationale

**Pros:**
- Maintains Spring ViewComponent's multi-engine support
- Clean separation of concerns
- Easy to add new engines later
- Core reactive logic reusable

**Cons:**
- More complex implementation
- Need adapters for each engine

### Architecture

```
spring-view-component-reactive-core
├─ @ReactiveViewComponent
├─ @Property
├─ @Action
├─ ComponentStateManager
├─ ReactiveViewComponentController
└─ ReactiveTemplateProcessor (interface)

spring-view-component-reactive-thymeleaf
└─ ThymeleafReactiveProcessor (implements ReactiveTemplateProcessor)

spring-view-component-reactive-jte
└─ JteReactiveProcessor (implements ReactiveTemplateProcessor)

spring-view-component-reactive-kte
└─ KteReactiveProcessor (implements ReactiveTemplateProcessor)
```

### Consequences
- More upfront work
- Better long-term maintainability
- Users can choose their preferred engine
- Consistent API across engines

---

## Decision 9: CSRF Protection by Default

### Context
How to protect reactive components from CSRF attacks?

### Options Considered

1. **Spring Security CSRF** - Reuse existing infrastructure
2. **Custom CSRF tokens**
3. **No CSRF protection** (rely on SameSite cookies)

### Decision
**Selected: Spring Security CSRF (Option 1)**

### Rationale

**Pros:**
- Reuses Spring Security infrastructure
- Automatic token generation and validation
- Works with existing Spring Security configurations
- Industry standard approach

**Cons:**
- Requires Spring Security dependency
- Slight complexity for developers

### Implementation

```java
@Configuration
public class ReactiveComponentSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers("/public/**")
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
        return http.build();
    }
}
```

Templates automatically include CSRF token:
```html
<div reactive:component="formComponent" reactive:csrf="${_csrf.token}">
    <!-- HTMX automatically includes token in requests -->
</div>
```

### Consequences
- Secure by default
- Familiar to Spring developers
- May need configuration for custom setups
- Works seamlessly with existing Spring Security

---

## Decision 10: Backwards Compatibility

### Context
How to ensure existing non-reactive components continue to work?

### Options Considered

1. **Separate namespace** - `@ReactiveViewComponent` distinct from `@ViewComponent`
2. **Migration required** - Break existing components
3. **Opt-in flags** - Add `reactive=true` parameter to `@ViewComponent`

### Decision
**Selected: Separate namespace (Option 1)**

### Rationale

**Pros:**
- Zero breaking changes
- Clear distinction between reactive and non-reactive
- Can coexist in same application
- Easy incremental adoption

**Cons:**
- Two component types to maintain
- Potential confusion for new developers

### Implementation

```java
// Existing components unchanged
@ViewComponent
public class StaticComponent {
    public SimpleView render() {
        return new SimpleView("Hello");
    }
}

// New reactive components opt-in
@ReactiveViewComponent
public class ReactiveComponent {
    @Property(reactive = true)
    private int count = 0;

    @Action
    public void increment() {
        count++;
    }
}
```

### Consequences
- Smooth migration path
- No breaking changes
- Clear intent in code
- May eventually deprecate `@ViewComponent` if reactive becomes standard

---

## Summary

These architectural decisions prioritize:

1. **Simplicity** - HTMX over WebSocket, Session over Redis by default
2. **Spring Integration** - Annotations, Bean Validation, Spring Security
3. **Type Safety** - Java static typing, compile-time checks
4. **Developer Experience** - Familiar patterns, minimal configuration
5. **Flexibility** - Multi-engine support, optional enhancements (Alpine.js, Kryo, Redis)
6. **Backwards Compatibility** - No breaking changes, incremental adoption

The decisions support a pragmatic, Spring-idiomatic approach to reactive components that balances power with simplicity.
