# Spring ViewComponent - Livewire Feature Implementation Specification

**Version:** 1.0
**Date:** 2026-01-15
**Target Version:** Spring ViewComponent 1.0.0+

---

## Executive Summary

This specification outlines multiple architectural approaches to bring Laravel Livewire-inspired reactive features to Spring ViewComponent. The goal is to enable reactive, interactive components with minimal JavaScript while maintaining Spring ViewComponent's core principles: type safety, Spring integration, and multi-template engine support.

---

## Table of Contents

1. [Feature Analysis](#1-feature-analysis)
2. [Core Requirements](#2-core-requirements)
3. [Architectural Options](#3-architectural-options)
4. [Feature Specifications](#4-feature-specifications)
5. [Implementation Roadmap](#5-implementation-roadmap)
6. [Technical Considerations](#6-technical-considerations)
7. [Migration Strategy](#7-migration-strategy)

---

## 1. Feature Analysis

### 1.1 Livewire Core Features

| Feature | Description | Priority | Complexity |
|---------|-------------|----------|------------|
| **Reactive Properties** | Two-way data binding between client and server | Critical | High |
| **Actions** | Server-side methods callable from client without page reload | Critical | High |
| **Component State** | Persistent state across requests | Critical | High |
| **Islands** | Isolated component regions that update independently | High | Very High |
| **Optimistic UI** | Immediate UI updates before server confirmation | Medium | Medium |
| **JavaScript Integration** | Hooks and refs for custom JS logic | High | Medium |
| **Form Validation** | Real-time validation with instant feedback | High | Medium |
| **File Uploads** | Reactive file upload with progress | Medium | High |
| **Loading States** | Automatic loading indicators | Medium | Low |
| **Polling** | Periodic component updates | Low | Low |
| **Scoped CSS/JS** | Component-scoped styles and scripts | Medium | Medium |
| **Single-File Components** | Optional single-file format | Low | Low |

### 1.2 Spring ViewComponent Current State

**Strengths:**
- ✅ Type-safe component contexts (Java Records)
- ✅ Spring dependency injection
- ✅ Multi-template engine support (Thymeleaf, JTE, KTE)
- ✅ Co-located templates
- ✅ Hot-reload in development
- ✅ AspectJ-based rendering pipeline

**Gaps for Reactive Features:**
- ❌ No client-side state management
- ❌ No action/event handling without page reload
- ❌ No component lifecycle management
- ❌ No partial rendering mechanism
- ❌ No WebSocket/SSE support
- ❌ No built-in JavaScript integration layer

---

## 2. Core Requirements

### 2.1 Functional Requirements

**FR1: Reactive Properties**
- Components must support properties that automatically sync between client and server
- Changes on client must trigger server updates
- Server updates must reflect in client DOM

**FR2: Action Methods**
- Components must expose methods callable from client-side events
- Actions must execute server-side without full page reload
- Actions must support parameters from client

**FR3: State Management**
- Component state must persist across multiple requests
- State must be serializable and deserializable
- Support for session-scoped and request-scoped state

**FR4: Partial Rendering**
- Components must support rendering only changed portions
- Must minimize network payload
- Must preserve client-side state (scroll position, focus, etc.)

**FR5: JavaScript Interoperability**
- Must provide hooks for custom JavaScript
- Must allow access to component state from JavaScript
- Must support JavaScript-triggered actions

### 2.2 Non-Functional Requirements

**NFR1: Type Safety**
- Maintain compile-time type safety for component properties
- Actions must have type-safe parameters
- Template bindings should have IDE support

**NFR2: Performance**
- Initial page load < 100ms overhead
- Action roundtrip < 200ms (excluding network)
- Memory usage < 10MB per 100 active component instances

**NFR3: Spring Compatibility**
- Must work with Spring Boot 3.x+
- Must integrate with Spring Security
- Must support Spring Session

**NFR4: Template Engine Neutrality**
- Core reactive features must work with all supported engines
- Engine-specific optimizations allowed but not required

**NFR5: Developer Experience**
- Minimal boilerplate code
- Clear error messages
- Comprehensive documentation
- Smooth migration path from non-reactive components

---

## 3. Architectural Options

### Option A: HTMX-Based Architecture (Recommended)

**Overview:**
Leverage HTMX for client-server communication, with Spring ViewComponent handling server-side rendering and state management.

#### Architecture Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                        Client Browser                        │
├─────────────────────────────────────────────────────────────┤
│  Component DOM                                               │
│  ├─ hx-get="/component/{id}/action"                         │
│  ├─ hx-post="/component/{id}/property"                      │
│  └─ hx-target="#component-{id}"                             │
│                                                              │
│  HTMX Library (included)                                     │
│  Alpine.js (optional for client-side logic)                  │
└─────────────────────────────────────────────────────────────┘
                            ▲  │
                            │  ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│  ReactiveViewComponentController                             │
│  ├─ POST /component/{id}/action/{method}                    │
│  ├─ POST /component/{id}/property/{name}                    │
│  └─ GET  /component/{id}/refresh                            │
│                                                              │
│  ComponentStateManager                                       │
│  ├─ StateStore (Redis/Session)                              │
│  ├─ Serialization/Deserialization                           │
│  └─ State Lifecycle Management                              │
│                                                              │
│  @ReactiveViewComponent                                      │
│  ├─ @Property (reactive properties)                          │
│  ├─ @Action (callable methods)                              │
│  └─ render() → IViewContext                                 │
│                                                              │
│  Template Engine (Thymeleaf/JTE/KTE)                        │
│  └─ Render component with reactive attributes                │
└─────────────────────────────────────────────────────────────┘
```

#### Component Example
```java
@ReactiveViewComponent
public class CounterComponent {

    @Property(reactive = true)
    private int count = 0;

    @Action
    public void increment() {
        count++;
    }

    @Action
    public void decrement() {
        count--;
    }

    public record CounterView(int count) implements ViewContext {}

    public CounterView render() {
        return new CounterView(count);
    }
}
```

#### Template Example (Thymeleaf)
```html
<div reactive:component="counterComponent">
    <h2>Count: <span th:text="${count}">0</span></h2>
    <button reactive:action="increment">+</button>
    <button reactive:action="decrement">-</button>
</div>
```

#### Pros
- ✅ Minimal JavaScript footprint (HTMX is ~14KB gzipped)
- ✅ Progressive enhancement friendly
- ✅ Works with existing Spring MVC infrastructure
- ✅ Easy to understand and debug
- ✅ Server-side rendering by default
- ✅ SEO friendly
- ✅ Strong TypeScript support via Alpine.js if needed

#### Cons
- ❌ Less suitable for highly interactive UIs (complex drag-drop, real-time collaboration)
- ❌ Network latency affects every interaction
- ❌ HTMX learning curve for developers
- ❌ Limited offline capability

#### Technical Details

**State Storage Options:**
1. **HTTP Session** (default) - Simple, works out of box
2. **Redis** - Distributed, scalable
3. **Database** - Persistent, queryable

**Serialization Strategy:**
- Jackson for JSON serialization
- Support for Java Records natively
- Custom serializers for complex types

**Request Flow:**
1. User clicks button with `reactive:action="increment"`
2. HTMX sends POST to `/component/{id}/action/increment`
3. Controller deserializes component state from session
4. Calls `increment()` method
5. Re-renders component
6. Returns HTML fragment
7. HTMX swaps DOM content

**Security:**
- CSRF protection via Spring Security
- Component ID signed with HMAC
- Action whitelist validation
- Rate limiting per component instance

---

### Option B: WebSocket-Based Architecture

**Overview:**
Use WebSockets for bidirectional communication, enabling real-time updates and lower latency.

#### Architecture Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                        Client Browser                        │
├─────────────────────────────────────────────────────────────┤
│  Component JavaScript Client                                 │
│  ├─ WebSocket connection to /ws/component/{id}              │
│  ├─ Message handlers (property-update, action-result)       │
│  └─ DOM manipulation library (Morphdom/Idiomorph)           │
│                                                              │
│  Alpine.js (for client-side logic)                           │
└─────────────────────────────────────────────────────────────┘
                            ▲  │
                            │  ▼ (WebSocket)
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│  ComponentWebSocketHandler                                   │
│  ├─ onOpen: Initialize component session                    │
│  ├─ onMessage: Handle actions, property updates             │
│  └─ onClose: Cleanup session                                │
│                                                              │
│  ComponentStateManager                                       │
│  ├─ In-memory state cache                                   │
│  ├─ Persistent state store (optional)                       │
│  └─ State synchronization                                   │
│                                                              │
│  @ReactiveViewComponent (same as Option A)                  │
│                                                              │
│  Template Engine                                             │
│  └─ Initial SSR + Client-side hydration                     │
└─────────────────────────────────────────────────────────────┘
```

#### Component Example
```java
@ReactiveViewComponent(transport = Transport.WEBSOCKET)
public class RealtimeChatComponent {

    @Property(reactive = true)
    private List<Message> messages = new ArrayList<>();

    @Property(reactive = true)
    private String newMessage = "";

    @Action
    public void sendMessage() {
        messages.add(new Message(newMessage, LocalDateTime.now()));
        newMessage = "";
        // Automatically broadcasts to all connected clients
    }

    @Action
    public void loadMore() {
        // Load more messages from database
    }

    public record ChatView(List<Message> messages, String newMessage) implements ViewContext {}

    public ChatView render() {
        return new ChatView(messages, newMessage);
    }
}
```

#### Pros
- ✅ Real-time bidirectional communication
- ✅ Lower latency than HTTP
- ✅ Efficient for high-frequency updates
- ✅ Supports broadcasting to multiple clients
- ✅ Better for collaborative features

#### Cons
- ❌ More complex infrastructure (load balancing, connection management)
- ❌ Higher memory usage (persistent connections)
- ❌ Requires Redis or similar for horizontal scaling
- ❌ More JavaScript on client side
- ❌ Harder to debug
- ❌ WebSocket firewall/proxy issues

#### Technical Details

**Message Protocol:**
```json
{
  "type": "action",
  "action": "sendMessage",
  "params": {},
  "componentId": "abc123"
}

{
  "type": "property-update",
  "property": "newMessage",
  "value": "Hello world",
  "componentId": "abc123"
}

{
  "type": "render-update",
  "html": "<div>...</div>",
  "componentId": "abc123"
}
```

**Connection Management:**
- Heartbeat every 30s
- Automatic reconnection with exponential backoff
- Session association via initial HTTP request
- Connection pooling and limits

**Scalability:**
- Redis Pub/Sub for multi-instance deployments
- Sticky sessions or shared state store
- WebSocket connection limits per node

---

### Option C: Hybrid HTTP + SSE Architecture

**Overview:**
Use HTTP POST for actions, Server-Sent Events (SSE) for server-to-client updates. Best of both worlds.

#### Architecture Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                        Client Browser                        │
├─────────────────────────────────────────────────────────────┤
│  Component JavaScript Client                                 │
│  ├─ EventSource (SSE) for server updates                    │
│  ├─ fetch() for actions                                     │
│  └─ DOM morphing for updates                                │
│                                                              │
│  Alpine.js (optional)                                        │
└─────────────────────────────────────────────────────────────┘
                ▲                           │
                │ (SSE)                     ▼ (HTTP POST)
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│  ReactiveViewComponentController (HTTP)                      │
│  ├─ POST /component/{id}/action/{method}                    │
│  └─ POST /component/{id}/property/{name}                    │
│                                                              │
│  ComponentSSEController                                      │
│  ├─ GET /component/{id}/events (SSE stream)                 │
│  └─ SseEmitter per component instance                       │
│                                                              │
│  ComponentStateManager                                       │
│  └─ Publishes state changes to SSE emitters                 │
│                                                              │
│  @ReactiveViewComponent (same as Option A)                  │
└─────────────────────────────────────────────────────────────┘
```

#### Component Example
```java
@ReactiveViewComponent(transport = Transport.HYBRID)
public class LiveDashboardComponent {

    @Property(reactive = true, pushUpdates = true)
    private DashboardMetrics metrics;

    @Scheduled(fixedRate = 5000)
    public void updateMetrics() {
        metrics = metricsService.getCurrentMetrics();
        // Automatically pushes to connected clients via SSE
    }

    @Action
    public void refreshNow() {
        updateMetrics();
    }

    public record DashboardView(DashboardMetrics metrics) implements ViewContext {}

    public DashboardView render() {
        return new DashboardView(metrics);
    }
}
```

#### Pros
- ✅ Server can push updates to client
- ✅ Simpler than WebSockets (HTTP-based)
- ✅ Better firewall/proxy compatibility than WebSocket
- ✅ Automatic reconnection built into EventSource
- ✅ Lower latency than polling
- ✅ Works with HTTP/2 multiplexing

#### Cons
- ❌ One-way server-to-client (actions still use HTTP)
- ❌ Browser connection limits (6 per domain)
- ❌ No binary data support
- ❌ Limited to text-based data

#### Technical Details

**SSE Message Format:**
```
event: component-update
data: {"componentId":"abc123","html":"<div>...</div>"}

event: property-change
data: {"componentId":"abc123","property":"count","value":42}
```

**Connection Management:**
- SseEmitter lifecycle tied to component instance
- Automatic cleanup on timeout/error
- Client reconnects automatically

---

### Option D: SPA-Style with Spring Data REST

**Overview:**
Full client-side rendering with React/Vue/Svelte, using Spring ViewComponent as a type-safe backend API generator.

#### Architecture Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                        Client Browser                        │
├─────────────────────────────────────────────────────────────┤
│  React/Vue/Svelte Component                                  │
│  ├─ Client-side state management                            │
│  ├─ Fetches data from component API                         │
│  └─ Calls action endpoints                                  │
│                                                              │
│  TypeScript generated from component definitions             │
└─────────────────────────────────────────────────────────────┘
                            ▲  │
                            │  ▼ (REST API)
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│  ComponentRestController                                     │
│  ├─ GET  /api/component/{id}/state                          │
│  ├─ POST /api/component/{id}/action/{method}                │
│  └─ Returns JSON                                            │
│                                                              │
│  @ReactiveViewComponent                                      │
│  ├─ Exposes properties as JSON                              │
│  └─ Actions return DTOs                                     │
│                                                              │
│  TypeScript Generator                                        │
│  └─ Generates TypeScript types from Java components         │
└─────────────────────────────────────────────────────────────┘
```

#### Pros
- ✅ Rich client-side interactivity
- ✅ Offline capabilities
- ✅ Type safety across stack (via generated TypeScript)
- ✅ Familiar to modern frontend developers
- ✅ Best performance for complex UIs

#### Cons
- ❌ Loses server-side rendering benefits
- ❌ SEO challenges (requires SSR setup)
- ❌ Increased complexity (separate frontend build)
- ❌ More JavaScript bundle size
- ❌ Strays from Spring ViewComponent's philosophy

---

## 4. Feature Specifications

### 4.1 Reactive Properties

#### 4.1.1 Property Definition

**Annotation-Based Approach:**
```java
@ReactiveViewComponent
public class FormComponent {

    @Property(
        reactive = true,           // Enable two-way binding
        lazy = false,              // Update on every change (vs. on blur)
        debounce = 300,            // Debounce updates (ms)
        persist = Persistence.SESSION  // Persistence strategy
    )
    private String username = "";

    @Property(reactive = true, lazy = true)
    private String email = "";
}
```

**Record-Based Approach (Alternative):**
```java
@ReactiveViewComponent
public class FormComponent {

    private ReactiveState state = new ReactiveState();

    public static class ReactiveState {
        public String username = "";
        public String email = "";
    }

    public record FormView(ReactiveState state) implements ViewContext {}
}
```

#### 4.1.2 Property Binding in Templates

**Thymeleaf:**
```html
<input type="text"
       reactive:model="username"
       reactive:lazy="false"
       reactive:debounce="300">
```

**JTE:**
```jte
<input type="text"
       data-reactive-model="username"
       data-reactive-debounce="300">
```

#### 4.1.3 Property Update Flow

```
User types in input
  ↓
Client-side debounce timer (300ms)
  ↓
POST /component/{id}/property/username
  Body: {"value": "john"}
  ↓
Server deserializes component state
  ↓
Validates property update
  ↓
Updates property value
  ↓
Serializes state back to session
  ↓
Optionally re-renders component
  ↓
Returns response (204 No Content or 200 with HTML)
  ↓
Client updates DOM if HTML returned
```

#### 4.1.4 Property Validation

```java
@ReactiveViewComponent
public class FormComponent {

    @Property(reactive = true)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20)
    private String username = "";

    @ValidationHandler
    public ValidationResult validateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            return ValidationResult.error("Username already taken");
        }
        return ValidationResult.ok();
    }
}
```

### 4.2 Actions

#### 4.2.1 Action Definition

```java
@ReactiveViewComponent
public class TodoComponent {

    @Property(reactive = true)
    private List<Todo> todos = new ArrayList<>();

    @Action
    public void addTodo(@ActionParam String text) {
        todos.add(new Todo(text));
    }

    @Action
    public void removeTodo(@ActionParam Long id) {
        todos.removeIf(t -> t.id().equals(id));
    }

    @Action
    public void toggleTodo(@ActionParam Long id) {
        todos.stream()
            .filter(t -> t.id().equals(id))
            .findFirst()
            .ifPresent(Todo::toggle);
    }

    @Action(async = true)
    public CompletableFuture<Void> fetchFromApi() {
        return todoApiService.fetchTodos()
            .thenAccept(fetchedTodos -> todos.addAll(fetchedTodos));
    }
}
```

#### 4.2.2 Action Invocation in Templates

**Thymeleaf:**
```html
<button reactive:click="addTodo('Buy milk')">Add Todo</button>
<button reactive:click="removeTodo(@{todo.id})">Remove</button>
<form reactive:submit="saveTodo">
    <input type="text" reactive:model="todoText">
    <button type="submit">Save</button>
</form>
```

**JTE:**
```jte
<button onclick="$reactive.call('addTodo', 'Buy milk')">Add Todo</button>
```

#### 4.2.3 Action Response Types

```java
// No return value - component re-renders
@Action
public void increment() {
    count++;
}

// Return ViewContext - render different component
@Action
public ViewContext submit() {
    return new SuccessPage();
}

// Return RedirectResponse - navigate to URL
@Action
public RedirectResponse save() {
    return RedirectResponse.to("/dashboard");
}

// Return JsonResponse - return data without rendering
@Action
@Json
public Map<String, Object> getData() {
    return Map.of("count", count);
}

// Return StreamResponse - streaming response
@Action
public StreamResponse generateReport() {
    return StreamResponse.from(reportGenerator.stream());
}
```

### 4.3 Component Lifecycle

```java
@ReactiveViewComponent
public class LifecycleComponent {

    @OnMount
    public void onMount() {
        // Called when component first renders
        // Initialize resources
    }

    @OnHydrate
    public void onHydrate() {
        // Called when component state is restored from storage
        // Restore transient state
    }

    @BeforeRender
    public void beforeRender() {
        // Called before every render
        // Update computed properties
    }

    @AfterRender
    public void afterRender() {
        // Called after render
        // Cleanup, logging
    }

    @OnDestroy
    public void onDestroy() {
        // Called when component session ends
        // Cleanup resources
    }
}
```

### 4.4 Islands (Isolated Rendering)

**Component Definition:**
```java
@ReactiveViewComponent
public class DashboardComponent {

    @Property(reactive = true)
    private UserProfile profile;

    @Property(reactive = true)
    private List<Notification> notifications;

    @Property(reactive = true)
    private AnalyticsData analytics;

    @Action
    @UpdateIsland("notifications")  // Only re-render notifications island
    public void markNotificationRead(@ActionParam Long id) {
        notifications.stream()
            .filter(n -> n.id().equals(id))
            .findFirst()
            .ifPresent(Notification::markRead);
    }

    @Action
    @UpdateIsland("analytics")
    public void refreshAnalytics() {
        analytics = analyticsService.fetchLatest();
    }
}
```

**Template:**
```html
<div reactive:component="dashboardComponent">
    <div reactive:island="profile">
        <!-- Profile section - won't re-render on notification updates -->
        <h2 th:text="${profile.name}"></h2>
    </div>

    <div reactive:island="notifications">
        <!-- Notifications - only updates when notifications change -->
        <div th:each="notif : ${notifications}">
            <span th:text="${notif.message}"></span>
            <button reactive:click="|markNotificationRead(${notif.id})|">
                Mark Read
            </button>
        </div>
    </div>

    <div reactive:island="analytics">
        <!-- Analytics - only updates when analytics change -->
        <div th:text="${analytics.pageViews}"></div>
    </div>
</div>
```

### 4.5 Optimistic UI Updates

```java
@ReactiveViewComponent
public class OptimisticComponent {

    @Property(reactive = true)
    private List<Comment> comments;

    @Action(optimistic = true)
    public void addComment(@ActionParam String text) {
        Comment newComment = new Comment(null, text, false);
        comments.add(newComment);

        // Save to database (async)
        Comment saved = commentRepository.save(newComment);

        // Update with real ID
        comments.set(comments.size() - 1, saved);
    }

    @Action(optimistic = true, rollbackOnError = true)
    public void likePost(@ActionParam Long postId) {
        // Optimistically update UI
        post.setLikes(post.getLikes() + 1);

        // If this throws, UI automatically rolls back
        apiService.likePost(postId);
    }
}
```

**Template:**
```html
<button reactive:click="addComment('Great post!')"
        reactive:optimistic="true">
    Add Comment
</button>

<!-- Shows loading state while action executes -->
<div reactive:loading>Saving...</div>

<!-- Shows on optimistic state -->
<div reactive:optimistic>Comment added!</div>

<!-- Shows on confirmed state -->
<div reactive:confirmed>Comment saved!</div>
```

### 4.6 JavaScript Integration

```java
@ReactiveViewComponent
public class ChartComponent {

    @Property(reactive = true)
    private ChartData data;

    @Action
    @Json  // Returns JSON instead of HTML
    public ChartData getData() {
        return data;
    }

    @Action
    public void updateData(@ActionParam @JsonBody ChartData newData) {
        this.data = newData;
    }
}
```

**Template with JavaScript:**
```html
<div reactive:component="chartComponent"
     x-data="chartComponent()">

    <canvas x-ref="chart"></canvas>

    <button @click="$reactive.call('updateData', generateRandomData())">
        Update Chart
    </button>
</div>

<script>
function chartComponent() {
    return {
        chart: null,

        init() {
            // Access component context
            const ctx = this.$refs.chart.getContext('2d');

            // Get initial data from server
            this.$reactive.call('getData').then(data => {
                this.chart = new Chart(ctx, {
                    type: 'bar',
                    data: data
                });
            });

            // Listen for server-side updates
            this.$reactive.on('data-updated', (newData) => {
                this.chart.data = newData;
                this.chart.update();
            });
        },

        generateRandomData() {
            return {
                labels: ['A', 'B', 'C'],
                datasets: [{
                    data: [Math.random(), Math.random(), Math.random()]
                }]
            };
        }
    }
}
</script>
```

### 4.7 Form Validation

```java
@ReactiveViewComponent
public class RegistrationComponent {

    @Property(reactive = true)
    @NotBlank
    @Size(min = 3, max = 20)
    private String username = "";

    @Property(reactive = true)
    @Email
    private String email = "";

    @Property(reactive = true)
    @Size(min = 8)
    private String password = "";

    // Real-time validation
    @ValidationHandler(property = "username", mode = ValidationMode.REALTIME)
    public ValidationResult validateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            return ValidationResult.error("Username already taken");
        }
        return ValidationResult.ok();
    }

    // Validation on blur
    @ValidationHandler(property = "email", mode = ValidationMode.LAZY)
    public ValidationResult validateEmail(String email) {
        if (email.endsWith("@example.com")) {
            return ValidationResult.warning("Example emails not recommended");
        }
        return ValidationResult.ok();
    }

    @Action
    public ViewContext register() {
        // Jakarta Bean Validation automatically applied
        User user = new User(username, email, password);
        userService.register(user);
        return new SuccessPage();
    }
}
```

**Template:**
```html
<form reactive:submit="register" reactive:validate>
    <div>
        <label>Username</label>
        <input type="text"
               reactive:model="username"
               reactive:validate:realtime>
        <span reactive:error="username" class="error"></span>
        <span reactive:loading="username">Checking...</span>
    </div>

    <div>
        <label>Email</label>
        <input type="email"
               reactive:model="email"
               reactive:validate:lazy>
        <span reactive:error="email" class="error"></span>
        <span reactive:warning="email" class="warning"></span>
    </div>

    <div>
        <label>Password</label>
        <input type="password"
               reactive:model="password">
        <span reactive:error="password" class="error"></span>
    </div>

    <button type="submit"
            reactive:disabled="hasErrors">
        Register
    </button>

    <div reactive:loading>
        Creating account...
    </div>
</form>
```

### 4.8 File Uploads

```java
@ReactiveViewComponent
public class FileUploadComponent {

    @Property(reactive = true)
    private List<UploadedFile> files = new ArrayList<>();

    @Property(reactive = true)
    private UploadProgress progress;

    @Action
    @FileUpload(
        maxSize = "10MB",
        allowedTypes = {"image/png", "image/jpeg", "application/pdf"},
        maxFiles = 5
    )
    public void handleUpload(@ActionParam MultipartFile file) {
        String url = fileStorageService.store(file);
        files.add(new UploadedFile(file.getOriginalFilename(), url));
    }

    @Action
    public void removeFile(@ActionParam String filename) {
        files.removeIf(f -> f.name().equals(filename));
    }
}
```

**Template:**
```html
<div reactive:component="fileUploadComponent">
    <input type="file"
           reactive:upload="handleUpload"
           reactive:upload:progress="progress"
           accept="image/*,.pdf"
           multiple>

    <div reactive:loading="handleUpload">
        <progress reactive:value="progress.percentage" max="100"></progress>
        <span th:text="${progress.percentage} + '%'"></span>
    </div>

    <ul>
        <li th:each="file : ${files}">
            <span th:text="${file.name}"></span>
            <button reactive:click="|removeFile('${file.name}')|">Remove</button>
        </li>
    </ul>
</div>
```

### 4.9 Loading States

```java
@ReactiveViewComponent
public class SearchComponent {

    @Action(
        loading = LoadingStrategy.BLOCK,  // Block UI during action
        loadingDelay = 200  // Show loading after 200ms
    )
    public void search(@ActionParam String query) {
        Thread.sleep(1000);  // Simulate slow search
        results = searchService.search(query);
    }
}
```

**Template:**
```html
<div reactive:component="searchComponent">
    <input type="text"
           reactive:model="query"
           reactive:keyup:debounce.500="search(query)">

    <!-- Shown immediately when action starts -->
    <div reactive:loading:inline>
        <span class="spinner"></span> Searching...
    </div>

    <!-- Blocks entire component during action -->
    <div reactive:loading:block>
        <div class="overlay">
            <span class="spinner"></span>
        </div>
    </div>

    <!-- Only shown if loading takes > 200ms -->
    <div reactive:loading:delay="200">
        This is taking a while...
    </div>

    <!-- Shown when specific action is executing -->
    <div reactive:loading:target="search">
        Searching database...
    </div>

    <!-- Results -->
    <ul reactive:loading:hide>
        <li th:each="result : ${results}" th:text="${result.title}"></li>
    </ul>
</div>
```

### 4.10 Polling

```java
@ReactiveViewComponent
public class StatusMonitorComponent {

    @Property(reactive = true)
    private ServerStatus status;

    @Poll(interval = 5000)  // Poll every 5 seconds
    public void checkStatus() {
        status = monitoringService.getStatus();
    }

    @Poll(interval = 1000, when = "status.isDeploying()")  // Conditional polling
    public void checkDeployment() {
        if (status.isDeploying()) {
            status = deploymentService.getStatus();
        }
    }
}
```

**Template:**
```html
<div reactive:component="statusMonitorComponent"
     reactive:poll.5s="checkStatus">

    <div th:classappend="${status.healthy ? 'green' : 'red'}">
        Status: <span th:text="${status.message}"></span>
    </div>

    <!-- Poll only when visible -->
    <div reactive:poll.1s="checkDeployment"
         reactive:poll:visible>
        Deployment progress: <span th:text="${status.progress}"></span>%
    </div>
</div>
```

### 4.11 Scoped CSS/JavaScript

**Component Definition:**
```java
@ReactiveViewComponent(
    styles = "styles.css",        // Co-located CSS file
    scripts = "behavior.js",       // Co-located JS file
    scopeStyles = true             // Scope CSS to component
)
public class CardComponent {
    // Component logic
}
```

**Directory Structure:**
```
src/main/java/com/example/components/
├── CardComponent.java
├── CardComponent.html        # Template
├── CardComponent.styles.css  # Scoped styles
└── CardComponent.behavior.js # Component script
```

**Scoped CSS (CardComponent.styles.css):**
```css
/* Automatically scoped to [data-component="card-abc123"] */
.card {
    border: 1px solid #ccc;
    padding: 1rem;
}

.card-title {
    font-size: 1.5rem;
    font-weight: bold;
}
```

**Generated CSS:**
```css
[data-component="card-abc123"] .card {
    border: 1px solid #ccc;
    padding: 1rem;
}

[data-component="card-abc123"] .card-title {
    font-size: 1.5rem;
    font-weight: bold;
}
```

**Component Script (CardComponent.behavior.js):**
```javascript
// `this` refers to component instance
export default {
    init() {
        console.log('Card component mounted', this.$el);
    },

    // Access reactive properties
    get title() {
        return this.$props.title;
    },

    // Call actions
    handleClick() {
        this.$reactive.call('onClick');
    }
}
```

---

## 5. Implementation Roadmap

### Phase 1: Foundation (Version 1.0.0-alpha)

**Goal:** Basic reactive properties and actions with HTMX

**Deliverables:**
1. `@ReactiveViewComponent` annotation
2. `@Property` annotation with basic reactivity
3. `@Action` annotation for methods
4. Component state serialization/deserialization
5. Session-based state storage
6. `ReactiveViewComponentController` for handling requests
7. HTMX integration
8. Basic Thymeleaf directives (`reactive:model`, `reactive:action`)

**Estimated Effort:** 4-6 weeks (1 developer)

**Success Criteria:**
- Counter component working with increment/decrement
- Form component with reactive inputs
- Basic todo list with add/remove

### Phase 2: Enhanced Interactivity (Version 1.0.0-beta)

**Goal:** Validation, loading states, and JavaScript integration

**Deliverables:**
1. Jakarta Bean Validation integration
2. `@ValidationHandler` annotation
3. Real-time and lazy validation
4. Loading state directives
5. `@Json` response type
6. JavaScript bridge (`$reactive` object)
7. Alpine.js integration examples
8. Error handling and display

**Estimated Effort:** 3-4 weeks

**Success Criteria:**
- Registration form with real-time validation
- Search component with loading states
- Chart component with JavaScript integration

### Phase 3: Advanced Features (Version 1.0.0-rc)

**Goal:** Islands, file uploads, polling

**Deliverables:**
1. Islands implementation (`@UpdateIsland`)
2. `@FileUpload` support with progress
3. `@Poll` annotation
4. Optimistic UI updates
5. Redis-based state storage option
6. Performance optimizations (differential rendering)

**Estimated Effort:** 4-5 weeks

**Success Criteria:**
- Dashboard with independent islands
- File upload with progress bar
- Live status monitor with polling

### Phase 4: Multi-Engine Support (Version 1.0.0)

**Goal:** Full support for JTE and KTE

**Deliverables:**
1. JTE reactive directives
2. KTE reactive directives
3. Engine-agnostic reactive core
4. Documentation for all engines
5. Example projects for each engine

**Estimated Effort:** 2-3 weeks

**Success Criteria:**
- All features work with Thymeleaf, JTE, and KTE
- Comprehensive documentation
- Migration guide

### Phase 5: Production Readiness (Version 1.1.0+)

**Goal:** Performance, security, and developer experience

**Deliverables:**
1. Security hardening (CSRF, rate limiting, input validation)
2. Performance profiling and optimization
3. Caching strategies
4. Horizontal scaling support (Redis Pub/Sub)
5. Developer tools (debugging, component inspector)
6. Comprehensive test suite
7. Real-world example applications

**Estimated Effort:** 6-8 weeks

---

## 6. Technical Considerations

### 6.1 State Serialization

**Challenge:** Java objects must be serializable across requests.

**Solutions:**

**Option 1: Jackson JSON Serialization**
```java
public class ComponentStateSerializer {
    private final ObjectMapper objectMapper;

    public String serialize(Object component) {
        return objectMapper.writeValueAsString(component);
    }

    public <T> T deserialize(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }
}
```

**Pros:** Simple, works with Records, human-readable
**Cons:** Larger payload, schema evolution challenges

**Option 2: Java Serialization**
```java
public class ComponentStateSerializer {
    public byte[] serialize(Object component) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(component);
        return baos.toByteArray();
    }
}
```

**Pros:** Fast, compact, handles complex objects
**Cons:** Security concerns, version compatibility issues, not human-readable

**Option 3: Kryo Serialization**
```java
public class ComponentStateSerializer {
    private final Kryo kryo = new Kryo();

    public byte[] serialize(Object component) {
        Output output = new Output(1024, -1);
        kryo.writeObject(output, component);
        return output.toBytes();
    }
}
```

**Pros:** Very fast, compact, better than Java serialization
**Cons:** Additional dependency, configuration needed

**Recommendation:** Jackson JSON for Phase 1-2, add Kryo option in Phase 3 for performance-critical applications.

### 6.2 Component Identity

**Challenge:** How to uniquely identify component instances across requests?

**Solution:**
```java
public class ComponentId {
    private final String componentClass;  // Fully qualified class name
    private final String sessionId;        // HTTP session ID
    private final String instanceId;       // UUID per instance
    private final String signature;        // HMAC signature for security

    public static ComponentId generate(Class<?> componentClass, HttpSession session) {
        String id = componentClass.getName() + ":" +
                    session.getId() + ":" +
                    UUID.randomUUID();
        String signature = computeHMAC(id);
        return new ComponentId(componentClass.getName(), session.getId(),
                               UUID.randomUUID().toString(), signature);
    }

    public String toUrlSafeString() {
        return Base64.getUrlEncoder().encodeToString(
            (componentClass + ":" + sessionId + ":" + instanceId + ":" + signature).getBytes()
        );
    }

    public boolean isValid() {
        return computeHMAC(componentClass + ":" + sessionId + ":" + instanceId)
                .equals(signature);
    }
}
```

### 6.3 Security Considerations

**CSRF Protection:**
```java
@Component
public class ReactiveComponentSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if (isReactiveComponentRequest(request)) {
            String csrfToken = request.getHeader("X-CSRF-Token");
            if (!csrfTokenRepository.validateToken(csrfToken, request.getSession())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        return true;
    }
}
```

**Rate Limiting:**
```java
@Aspect
@Component
public class RateLimitAspect {

    @Around("@annotation(Action)")
    public Object rateLimit(ProceedingJoinPoint pjp) throws Throwable {
        ComponentId componentId = getComponentId();
        String key = "ratelimit:" + componentId.toString();

        Long requests = redisTemplate.opsForValue().increment(key);
        if (requests == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        if (requests > 100) {  // 100 requests per minute
            throw new RateLimitExceededException();
        }

        return pjp.proceed();
    }
}
```

**Action Whitelisting:**
```java
public class ActionSecurityValidator {

    public void validateAction(String actionName, Class<?> componentClass) {
        Method method = findMethod(componentClass, actionName);

        if (method == null || !method.isAnnotationPresent(Action.class)) {
            throw new UnauthorizedActionException(
                "Action " + actionName + " is not exposed"
            );
        }

        Action annotation = method.getAnnotation(Action.class);
        if (annotation.requiresAuth() && !isAuthenticated()) {
            throw new UnauthorizedActionException("Authentication required");
        }
    }
}
```

### 6.4 Performance Optimization

**Differential Rendering:**
```java
public class DifferentialRenderer {

    public String renderDiff(String oldHtml, String newHtml) {
        // Use Morphdom algorithm to compute minimal DOM operations
        List<DomOperation> operations = computeDiff(oldHtml, newHtml);

        // Return JSON instructions instead of full HTML
        return serializeOperations(operations);
    }

    private List<DomOperation> computeDiff(String oldHtml, String newHtml) {
        Document oldDoc = Jsoup.parse(oldHtml);
        Document newDoc = Jsoup.parse(newHtml);

        // Simplified algorithm
        List<DomOperation> ops = new ArrayList<>();
        diffNodes(oldDoc.body(), newDoc.body(), ops);
        return ops;
    }
}
```

**Response Format:**
```json
{
  "operations": [
    {"type": "setAttribute", "selector": "#count", "attr": "textContent", "value": "42"},
    {"type": "removeClass", "selector": ".btn", "class": "disabled"},
    {"type": "insertAfter", "selector": "#item-2", "html": "<li>New item</li>"}
  ]
}
```

**Component Caching:**
```java
@ReactiveViewComponent
public class ExpensiveComponent {

    @Cacheable(value = "componentCache", key = "#root.target.componentId")
    public ExpensiveView render() {
        // Expensive computation
        return new ExpensiveView(expensiveService.compute());
    }

    @Action
    @CacheEvict(value = "componentCache", key = "#root.target.componentId")
    public void refresh() {
        // Evicts cache on action
    }
}
```

### 6.5 Template Engine Integration

**Engine-Agnostic Core:**
```java
public interface ReactiveTemplateProcessor {

    boolean supportsEngine(String engineName);

    void registerDirectives();

    String processReactiveAttributes(String template, ComponentMetadata metadata);

    String renderComponent(Object component);
}

@Component
public class ThymeleafReactiveProcessor implements ReactiveTemplateProcessor {

    @Override
    public boolean supportsEngine(String engineName) {
        return "thymeleaf".equals(engineName);
    }

    @Override
    public void registerDirectives() {
        // Register custom Thymeleaf processors
    }
}

@Component
public class JteReactiveProcessor implements ReactiveTemplateProcessor {

    @Override
    public boolean supportsEngine(String engineName) {
        return "jte".equals(engineName);
    }

    @Override
    public void registerDirectives() {
        // Register JTE extensions
    }
}
```

### 6.6 Horizontal Scaling

**Stateless Architecture with Redis:**
```java
@Configuration
public class ReactiveComponentConfig {

    @Bean
    public ComponentStateStore stateStore(RedisTemplate<String, String> redisTemplate) {
        return new RedisComponentStateStore(redisTemplate);
    }
}

public class RedisComponentStateStore implements ComponentStateStore {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveState(ComponentId id, Object component) {
        String key = "component:" + id.toString();
        String json = serialize(component);
        redisTemplate.opsForValue().set(key, json, 30, TimeUnit.MINUTES);
    }

    @Override
    public <T> T loadState(ComponentId id, Class<T> componentClass) {
        String key = "component:" + id.toString();
        String json = redisTemplate.opsForValue().get(key);
        return deserialize(json, componentClass);
    }
}
```

**Session Affinity Alternative:**
```yaml
# AWS Application Load Balancer
TargetGroup:
  Stickiness:
    Enabled: true
    Type: lb_cookie
    Duration: 86400  # 24 hours
```

---

## 7. Migration Strategy

### 7.1 Backward Compatibility

**Goal:** Existing non-reactive components continue to work without changes.

**Strategy:**
- `@ViewComponent` components work as before
- `@ReactiveViewComponent` is opt-in
- Shared infrastructure (aspects, handlers, etc.)
- No breaking changes to existing APIs

### 7.2 Incremental Adoption

**Step 1: Start with Simple Components**
```java
// Before
@ViewComponent
public class CounterComponent {
    public record CounterView(int count) implements ViewContext {}

    public CounterView render(int count) {
        return new CounterView(count);
    }
}

// After
@ReactiveViewComponent
public class CounterComponent {
    @Property(reactive = true)
    private int count = 0;

    @Action
    public void increment() {
        count++;
    }

    public record CounterView(int count) implements ViewContext {}

    public CounterView render() {
        return new CounterView(count);
    }
}
```

**Step 2: Add Interactivity to Forms**
```java
@ReactiveViewComponent
public class ContactFormComponent {
    @Property(reactive = true)
    private ContactForm form = new ContactForm();

    @Action
    public ViewContext submit() {
        contactService.sendMessage(form);
        return new SuccessPage();
    }
}
```

**Step 3: Leverage Advanced Features**
```java
@ReactiveViewComponent
public class DashboardComponent {
    @Property(reactive = true)
    @UpdateIsland("metrics")
    private Metrics metrics;

    @Poll(interval = 5000)
    public void refreshMetrics() {
        metrics = metricsService.getLatest();
    }
}
```

### 7.3 Migration Tools

**Component Analyzer:**
```java
public class ReactiveComponentMigrationAnalyzer {

    public MigrationReport analyze(Class<?> componentClass) {
        // Identifies:
        // 1. Stateful fields that should be @Property
        // 2. Methods that should be @Action
        // 3. Potential lifecycle hooks
        // 4. Validation annotations
    }
}
```

**Code Generator:**
```bash
$ ./gradlew generateReactiveComponent \
    --source=src/main/java/com/example/CounterComponent.java \
    --target=src/main/java/com/example/ReactiveCounterComponent.java
```

---

## 8. Comparison Matrix

### Feature Comparison: Spring ViewComponent vs. Laravel Livewire

| Feature | Livewire 4 | Spring VC (Proposed) | Gap |
|---------|-----------|---------------------|-----|
| **Reactive Properties** | ✅ Public PHP properties | ✅ `@Property` annotation | Equal |
| **Actions** | ✅ Public methods | ✅ `@Action` annotation | Equal |
| **Component State** | ✅ Automatic serialization | ✅ Configurable serialization | Equal |
| **Islands** | ✅ Native support | ✅ `@UpdateIsland` | Equal |
| **Optimistic UI** | ✅ Built-in directives | ✅ `optimistic=true` | Equal |
| **JavaScript Integration** | ✅ Alpine.js + wire:ref | ✅ Alpine.js + $reactive | Equal |
| **Form Validation** | ✅ Laravel validators | ✅ Jakarta Bean Validation | Better (type-safe) |
| **File Uploads** | ✅ With progress | ✅ `@FileUpload` + progress | Equal |
| **Loading States** | ✅ wire:loading | ✅ reactive:loading | Equal |
| **Polling** | ✅ wire:poll | ✅ `@Poll` / reactive:poll | Equal |
| **Scoped CSS** | ✅ Automatic scoping | ✅ scopeStyles=true | Equal |
| **Single-File Components** | ✅ With ⚡ prefix | ⚠️ Future enhancement | Minor gap |
| **Type Safety** | ❌ PHP dynamic typing | ✅ Java static typing | Better |
| **IDE Support** | ⚠️ Mixed (PHPStorm) | ✅ Full Java/Kotlin support | Better |
| **Multi-Engine Support** | ❌ Blade only | ✅ Thymeleaf/JTE/KTE | Better |
| **Performance** | ⚠️ Interpreter overhead | ✅ Compiled bytecode | Better |

---

## 9. Recommended Architecture: Option A (HTMX-Based)

### Why Option A?

1. **Simplicity:** HTMX provides a thin client-side layer with minimal JavaScript
2. **Progressive Enhancement:** Works without JavaScript (graceful degradation)
3. **Spring Integration:** Leverages existing Spring MVC infrastructure
4. **Type Safety:** Full Java type system throughout
5. **SEO Friendly:** Server-side rendered by default
6. **Learning Curve:** Easier for Spring developers (no complex frontend framework)
7. **Performance:** Fast initial load, efficient partial updates
8. **Maintainability:** Less code surface area, fewer dependencies

### Architecture Summary

```
Client (Browser)
├─ HTMX (14KB) - handles server communication
├─ Alpine.js (optional, 15KB) - for complex client-side logic
└─ Morphdom (2KB) - for efficient DOM updates

Spring Boot Application
├─ @ReactiveViewComponent - component definitions
├─ ReactiveViewComponentController - HTTP endpoints
├─ ComponentStateManager - state persistence
├─ Template Engines - rendering
└─ Security/Validation - cross-cutting concerns
```

### Next Steps for Implementation

1. **Create POC** with basic counter component
2. **Implement core annotations** (`@ReactiveViewComponent`, `@Property`, `@Action`)
3. **Build state management** layer
4. **Integrate HTMX** with custom attributes
5. **Add Thymeleaf directives**
6. **Write comprehensive tests**
7. **Create documentation** and examples

---

## 10. Open Questions

1. **Should we support real-time multi-user collaboration?**
   - Requires WebSocket (Option B) or SSE (Option C)
   - Adds significant complexity
   - Use case: collaborative editors, live chat

2. **How to handle very large component state?**
   - Option: Lazy load properties
   - Option: Compress state in Redis
   - Option: Store state in database with caching layer

3. **Should components be versioned?**
   - Useful for gradual deployments
   - Allows state migration on schema changes
   - Adds complexity to serialization

4. **Integration with Spring WebFlux?**
   - Reactive streams for async operations
   - Non-blocking I/O
   - Requires different architecture (reactive handlers)

5. **Support for micro-frontends?**
   - Components as independent deployable units
   - Cross-component communication
   - Shared state management

6. **Developer tooling priorities?**
   - Component inspector (browser extension)
   - Performance profiler
   - Time-travel debugging
   - State visualization

---

## 11. Conclusion

Bringing Livewire-inspired reactive features to Spring ViewComponent is highly feasible and would provide significant value to the Spring ecosystem. The HTMX-based architecture (Option A) offers the best balance of simplicity, performance, and Spring integration.

**Key Advantages:**
- Maintains Spring ViewComponent's type safety and Spring integration
- Minimal JavaScript footprint
- Progressive enhancement
- Multi-template engine support
- Familiar development model for Java developers

**Recommended Approach:**
1. Start with Phase 1 (Foundation) - basic reactive properties and actions
2. Gather community feedback
3. Iterate through phases based on user demand
4. Maintain backward compatibility throughout

**Success Metrics:**
- Adoption rate among Spring ViewComponent users
- Performance benchmarks vs. traditional Spring MVC
- Developer satisfaction (surveys, GitHub issues)
- Community contributions

This specification provides a clear path forward for implementing Livewire-like features while staying true to Spring ViewComponent's principles and the Java ecosystem's strengths.
