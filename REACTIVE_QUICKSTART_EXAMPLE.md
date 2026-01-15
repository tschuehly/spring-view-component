# Reactive Spring ViewComponent - Quick Start Example

This document shows what the proposed reactive API would look like in practice.

## Example 1: Counter Component (Hello World)

### Component Class

```java
package com.example.components;

import de.tschuehly.spring.viewcomponent.core.component.ViewContext;
import de.tschuehly.spring.viewcomponent.reactive.ReactiveViewComponent;
import de.tschuehly.spring.viewcomponent.reactive.Property;
import de.tschuehly.spring.viewcomponent.reactive.Action;

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

    @Action
    public void reset() {
        count = 0;
    }

    public record CounterView(int count) implements ViewContext {}

    public CounterView render() {
        return new CounterView(count);
    }
}
```

### Template (Thymeleaf)

```html
<!-- CounterComponent.html -->
<div reactive:component="counterComponent" class="counter">
    <h2>Counter Example</h2>

    <div class="display">
        <span class="count" th:text="${count}">0</span>
    </div>

    <div class="controls">
        <button reactive:click="decrement" class="btn">-</button>
        <button reactive:click="reset" class="btn">Reset</button>
        <button reactive:click="increment" class="btn">+</button>
    </div>

    <div reactive:loading>
        <span class="spinner"></span> Updating...
    </div>
</div>
```

### Controller

```java
@Controller
public class HomeController {

    private final CounterComponent counterComponent;

    public HomeController(CounterComponent counterComponent) {
        this.counterComponent = counterComponent;
    }

    @GetMapping("/counter")
    public ViewContext counter() {
        return counterComponent.render();
    }
}
```

---

## Example 2: Todo List Component

### Component Class

```java
@ReactiveViewComponent
public class TodoListComponent {

    @Property(reactive = true)
    private List<Todo> todos = new ArrayList<>();

    @Property(reactive = true)
    private String newTodoText = "";

    @Action
    public void addTodo() {
        if (!newTodoText.isBlank()) {
            todos.add(new Todo(
                UUID.randomUUID().toString(),
                newTodoText,
                false,
                LocalDateTime.now()
            ));
            newTodoText = ""; // Clear input
        }
    }

    @Action
    public void toggleTodo(@ActionParam String id) {
        todos.stream()
            .filter(t -> t.id().equals(id))
            .findFirst()
            .ifPresent(Todo::toggle);
    }

    @Action
    public void deleteTodo(@ActionParam String id) {
        todos.removeIf(t -> t.id().equals(id));
    }

    @Action
    public void clearCompleted() {
        todos.removeIf(Todo::completed);
    }

    public record TodoView(
        List<Todo> todos,
        String newTodoText,
        long completedCount,
        long activeCount
    ) implements ViewContext {}

    public TodoView render() {
        long completed = todos.stream().filter(Todo::completed).count();
        long active = todos.size() - completed;
        return new TodoView(todos, newTodoText, completed, active);
    }

    public record Todo(String id, String text, boolean completed, LocalDateTime createdAt) {
        public void toggle() {
            // In real implementation, would update the actual object
        }
    }
}
```

### Template (Thymeleaf)

```html
<!-- TodoListComponent.html -->
<div reactive:component="todoListComponent" class="todo-app">
    <h1>My Todos</h1>

    <!-- Add Todo Form -->
    <form reactive:submit="addTodo" class="todo-form">
        <input
            type="text"
            reactive:model="newTodoText"
            reactive:model:lazy="false"
            placeholder="What needs to be done?"
            class="todo-input"
        />
        <button type="submit" class="btn-add">Add</button>
    </form>

    <!-- Todo List -->
    <ul class="todo-list">
        <li th:each="todo : ${todos}"
            th:classappend="${todo.completed} ? 'completed' : ''"
            class="todo-item">

            <input
                type="checkbox"
                th:checked="${todo.completed}"
                reactive:click="|toggleTodo('${todo.id}')|"
            />

            <span class="todo-text" th:text="${todo.text}">Task</span>

            <button
                reactive:click="|deleteTodo('${todo.id}')|"
                class="btn-delete">
                ×
            </button>
        </li>
    </ul>

    <!-- Footer -->
    <div class="todo-footer">
        <span class="todo-count">
            <strong th:text="${activeCount}">0</strong>
            <span th:text="${activeCount == 1 ? 'item' : 'items'}">items</span> left
        </span>

        <button
            reactive:click="clearCompleted"
            th:if="${completedCount > 0}"
            class="btn-clear">
            Clear completed (<span th:text="${completedCount}">0</span>)
        </button>
    </div>

    <!-- Loading Indicator -->
    <div reactive:loading class="loading-overlay">
        <span class="spinner"></span>
    </div>
</div>
```

---

## Example 3: Search Component with Debouncing

### Component Class

```java
@ReactiveViewComponent
public class SearchComponent {

    private final ProductRepository productRepository;

    @Property(reactive = true, debounce = 300)
    private String query = "";

    @Property(reactive = true)
    private List<Product> results = new ArrayList<>();

    @Property(reactive = true)
    private boolean searching = false;

    public SearchComponent(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Action
    public void search() {
        searching = true;
        if (query.length() >= 3) {
            results = productRepository.searchByName(query);
        } else {
            results = new ArrayList<>();
        }
        searching = false;
    }

    public record SearchView(
        String query,
        List<Product> results,
        boolean searching
    ) implements ViewContext {}

    public SearchView render() {
        return new SearchView(query, results, searching);
    }
}
```

### Template (Thymeleaf)

```html
<!-- SearchComponent.html -->
<div reactive:component="searchComponent" class="search">
    <div class="search-box">
        <input
            type="search"
            reactive:model="query"
            reactive:model:debounce="300"
            reactive:keyup="search"
            placeholder="Search products..."
            class="search-input"
        />

        <div reactive:loading:target="search" class="search-spinner">
            <span class="spinner"></span>
        </div>
    </div>

    <div class="results" th:if="${!results.isEmpty()}">
        <h3>Results (<span th:text="${results.size()}">0</span>)</h3>
        <ul>
            <li th:each="product : ${results}" class="result-item">
                <img th:src="${product.imageUrl}" th:alt="${product.name}" />
                <div class="result-info">
                    <h4 th:text="${product.name}">Product Name</h4>
                    <p th:text="${product.description}">Description</p>
                    <span class="price" th:text="'$' + ${product.price}">$0.00</span>
                </div>
            </li>
        </ul>
    </div>

    <div class="no-results" th:if="${!query.isBlank() && results.isEmpty() && !searching}">
        No results found for "<span th:text="${query}"></span>"
    </div>
</div>
```

---

## Example 4: Form with Validation

### Component Class

```java
@ReactiveViewComponent
public class ContactFormComponent {

    private final EmailService emailService;

    @Property(reactive = true)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name = "";

    @Property(reactive = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email = "";

    @Property(reactive = true)
    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 500, message = "Message must be between 10 and 500 characters")
    private String message = "";

    @Property(reactive = true)
    private boolean submitted = false;

    public ContactFormComponent(EmailService emailService) {
        this.emailService = emailService;
    }

    @ValidationHandler(property = "email", mode = ValidationMode.LAZY)
    public ValidationResult validateEmail(String email) {
        if (email.endsWith("@example.com")) {
            return ValidationResult.warning("Please use your real email address");
        }
        return ValidationResult.ok();
    }

    @Action
    public void submit() {
        emailService.sendContactEmail(name, email, message);
        submitted = true;

        // Reset form
        name = "";
        email = "";
        message = "";
    }

    public record ContactView(
        String name,
        String email,
        String message,
        boolean submitted
    ) implements ViewContext {}

    public ContactView render() {
        return new ContactView(name, email, message, submitted);
    }
}
```

### Template (Thymeleaf)

```html
<!-- ContactFormComponent.html -->
<div reactive:component="contactFormComponent" class="contact-form">
    <h2>Contact Us</h2>

    <div th:if="${submitted}" class="alert alert-success">
        Thank you for your message! We'll get back to you soon.
    </div>

    <form reactive:submit="submit" reactive:validate>
        <div class="form-group">
            <label for="name">Name</label>
            <input
                id="name"
                type="text"
                reactive:model="name"
                reactive:model:lazy="true"
                class="form-control"
            />
            <span reactive:error="name" class="error-message"></span>
        </div>

        <div class="form-group">
            <label for="email">Email</label>
            <input
                id="email"
                type="email"
                reactive:model="email"
                reactive:model:lazy="true"
                reactive:validate:lazy
                class="form-control"
            />
            <span reactive:error="email" class="error-message"></span>
            <span reactive:warning="email" class="warning-message"></span>
            <span reactive:loading="email" class="validating">Validating...</span>
        </div>

        <div class="form-group">
            <label for="message">Message</label>
            <textarea
                id="message"
                reactive:model="message"
                reactive:model:lazy="true"
                rows="5"
                class="form-control"
            ></textarea>
            <span reactive:error="message" class="error-message"></span>
            <div class="char-count">
                <span th:text="${message.length()}">0</span> / 500
            </div>
        </div>

        <button
            type="submit"
            reactive:disabled="hasErrors"
            class="btn btn-primary">
            Send Message
        </button>

        <div reactive:loading:target="submit" class="submitting">
            Sending your message...
        </div>
    </form>
</div>
```

---

## Example 5: Dashboard with Islands

### Component Class

```java
@ReactiveViewComponent
public class DashboardComponent {

    private final MetricsService metricsService;
    private final NotificationService notificationService;
    private final UserService userService;

    @Property(reactive = true)
    private UserProfile profile;

    @Property(reactive = true)
    private List<Notification> notifications;

    @Property(reactive = true)
    private DashboardMetrics metrics;

    public DashboardComponent(
        MetricsService metricsService,
        NotificationService notificationService,
        UserService userService
    ) {
        this.metricsService = metricsService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @OnMount
    public void initialize() {
        profile = userService.getCurrentUserProfile();
        notifications = notificationService.getRecentNotifications();
        metrics = metricsService.getCurrentMetrics();
    }

    @Action
    @UpdateIsland("notifications")
    public void markNotificationRead(@ActionParam Long id) {
        notificationService.markAsRead(id);
        notifications = notificationService.getRecentNotifications();
    }

    @Action
    @UpdateIsland("metrics")
    public void refreshMetrics() {
        metrics = metricsService.getCurrentMetrics();
    }

    @Action
    @UpdateIsland("profile")
    public void updateProfile(@ActionParam String name) {
        profile = userService.updateProfile(name);
    }

    @Poll(interval = 30000) // Poll every 30 seconds
    @UpdateIsland("notifications")
    public void checkNewNotifications() {
        notifications = notificationService.getRecentNotifications();
    }

    public record DashboardView(
        UserProfile profile,
        List<Notification> notifications,
        DashboardMetrics metrics
    ) implements ViewContext {}

    public DashboardView render() {
        return new DashboardView(profile, notifications, metrics);
    }
}
```

### Template (Thymeleaf)

```html
<!-- DashboardComponent.html -->
<div reactive:component="dashboardComponent" class="dashboard">
    <h1>Dashboard</h1>

    <div class="dashboard-grid">
        <!-- Profile Island - Independent updates -->
        <div reactive:island="profile" class="card profile-card">
            <h2>Profile</h2>
            <img th:src="${profile.avatarUrl}" alt="Avatar" class="avatar" />
            <h3 th:text="${profile.name}">User Name</h3>
            <p th:text="${profile.email}">user@example.com</p>

            <form reactive:submit="updateProfile">
                <input type="text" reactive:model="profile.name" />
                <button type="submit">Update</button>
            </form>
        </div>

        <!-- Notifications Island - Polls for updates -->
        <div reactive:island="notifications"
             reactive:poll.30s="checkNewNotifications"
             class="card notifications-card">
            <h2>
                Notifications
                <span class="badge" th:text="${notifications.size()}">0</span>
            </h2>

            <ul class="notification-list">
                <li th:each="notif : ${notifications}"
                    th:classappend="${notif.unread} ? 'unread' : ''"
                    class="notification-item">

                    <div class="notification-content">
                        <strong th:text="${notif.title}">Title</strong>
                        <p th:text="${notif.message}">Message</p>
                        <small th:text="${notif.timestamp}">Just now</small>
                    </div>

                    <button
                        th:if="${notif.unread}"
                        reactive:click="|markNotificationRead(${notif.id})|"
                        class="btn-mark-read">
                        Mark as read
                    </button>
                </li>
            </ul>

            <div reactive:loading:target="checkNewNotifications">
                Checking for new notifications...
            </div>
        </div>

        <!-- Metrics Island - Manual refresh only -->
        <div reactive:island="metrics" class="card metrics-card">
            <div class="card-header">
                <h2>Metrics</h2>
                <button reactive:click="refreshMetrics" class="btn-refresh">
                    Refresh
                </button>
            </div>

            <div class="metrics-grid">
                <div class="metric">
                    <span class="metric-label">Page Views</span>
                    <span class="metric-value" th:text="${metrics.pageViews}">0</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Active Users</span>
                    <span class="metric-value" th:text="${metrics.activeUsers}">0</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Conversion Rate</span>
                    <span class="metric-value" th:text="${metrics.conversionRate} + '%'">0%</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Revenue</span>
                    <span class="metric-value" th:text="'$' + ${metrics.revenue}">$0</span>
                </div>
            </div>

            <div reactive:loading:target="refreshMetrics">
                Updating metrics...
            </div>
        </div>
    </div>
</div>
```

---

## Example 6: File Upload Component

### Component Class

```java
@ReactiveViewComponent
public class FileUploadComponent {

    private final FileStorageService fileStorageService;

    @Property(reactive = true)
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    @Property(reactive = true)
    private UploadProgress currentProgress = null;

    public FileUploadComponent(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Action
    @FileUpload(
        maxSize = "10MB",
        allowedTypes = {"image/png", "image/jpeg", "application/pdf"},
        maxFiles = 5
    )
    public void handleUpload(
        @ActionParam MultipartFile file,
        @ActionParam UploadProgress progress
    ) {
        currentProgress = progress;

        String url = fileStorageService.store(file, (bytesWritten, totalBytes) -> {
            progress.update(bytesWritten, totalBytes);
        });

        uploadedFiles.add(new UploadedFile(
            file.getOriginalFilename(),
            file.getSize(),
            url,
            LocalDateTime.now()
        ));

        currentProgress = null;
    }

    @Action
    public void removeFile(@ActionParam String url) {
        fileStorageService.delete(url);
        uploadedFiles.removeIf(f -> f.url().equals(url));
    }

    public record FileUploadView(
        List<UploadedFile> uploadedFiles,
        UploadProgress currentProgress
    ) implements ViewContext {}

    public FileUploadView render() {
        return new FileUploadView(uploadedFiles, currentProgress);
    }

    public record UploadedFile(
        String filename,
        long size,
        String url,
        LocalDateTime uploadedAt
    ) {}
}
```

### Template (Thymeleaf)

```html
<!-- FileUploadComponent.html -->
<div reactive:component="fileUploadComponent" class="file-upload">
    <h2>File Upload</h2>

    <div class="upload-area">
        <input
            type="file"
            reactive:upload="handleUpload"
            reactive:upload:progress="currentProgress"
            accept="image/*,.pdf"
            multiple
            class="file-input"
        />

        <div th:if="${currentProgress != null}" class="upload-progress">
            <div class="progress-bar">
                <div class="progress-fill"
                     th:style="'width: ' + ${currentProgress.percentage} + '%'">
                </div>
            </div>
            <span class="progress-text">
                Uploading <span th:text="${currentProgress.filename}">file</span>...
                <strong th:text="${currentProgress.percentage}">0</strong>%
            </span>
        </div>
    </div>

    <div class="uploaded-files">
        <h3>Uploaded Files (<span th:text="${uploadedFiles.size()}">0</span>)</h3>

        <ul>
            <li th:each="file : ${uploadedFiles}" class="file-item">
                <div class="file-icon">
                    <img th:if="${file.filename.endsWith('.png') or file.filename.endsWith('.jpg')}"
                         th:src="${file.url}"
                         alt="Preview"
                         class="file-thumbnail" />
                    <span th:unless="${file.filename.endsWith('.png') or file.filename.endsWith('.jpg')}"
                          class="icon-pdf">PDF</span>
                </div>

                <div class="file-info">
                    <strong th:text="${file.filename}">filename.pdf</strong>
                    <small th:text="${#numbers.formatDecimal(file.size / 1024, 1, 2)} + ' KB'">0 KB</small>
                    <small th:text="${file.uploadedAt}">Just now</small>
                </div>

                <button reactive:click="|removeFile('${file.url}')|"
                        class="btn-remove">
                    Remove
                </button>
            </li>
        </ul>
    </div>
</div>
```

---

## Application Configuration

### application.yml

```yaml
spring:
  view-component:
    local-development: true
    reactive:
      enabled: true
      transport: htmx  # htmx, websocket, or sse
      state-storage: session  # session, redis, or database
      csrf-protection: true
      rate-limiting:
        enabled: true
        requests-per-minute: 100
      session-timeout: 30m
      enable-islands: true
      enable-optimistic-ui: true

    # HTMX Configuration
    htmx:
      version: 2.0.0
      include-alpine: true
      alpine-version: 3.13.0

    # Redis Configuration (if using Redis state storage)
    redis:
      host: localhost
      port: 6379
      timeout: 30m
```

### Build Configuration (build.gradle)

```gradle
dependencies {
    // Existing dependencies
    implementation 'de.tschuehly:spring-view-component-core:0.9.0'
    implementation 'de.tschuehly:spring-view-component-thymeleaf:0.9.0'

    // New reactive module
    implementation 'de.tschuehly:spring-view-component-reactive:1.0.0'

    // HTMX and Alpine.js (included via WebJars)
    implementation 'org.webjars.npm:htmx.org:2.0.0'
    implementation 'org.webjars.npm:alpinejs:3.13.0'

    // Optional: Redis for distributed state
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.session:spring-session-data-redis'
}
```

---

## Summary

This quick start guide demonstrates how the proposed reactive API would work in practice. Key features:

1. **Simple annotation-based API** - `@ReactiveViewComponent`, `@Property`, `@Action`
2. **Minimal template changes** - `reactive:` attributes for interactivity
3. **Type-safe** - Full Java type system, IDE support
4. **Progressive enhancement** - Works without JavaScript
5. **Flexible** - Islands, polling, validation, file uploads, etc.

The examples show increasing complexity from a simple counter to a full dashboard with independent islands, demonstrating the power and flexibility of the proposed architecture.
