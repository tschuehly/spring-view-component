# Specification: Relative Resource Resolution for ViewComponents

## 1. Overview

### 1.1 Purpose
Enable developers to place static resources (JavaScript, CSS, images) in the same package as their ViewComponent classes and reference them using relative paths in templates. This creates a cohesive, component-based architecture where all assets related to a component live together.

### 1.2 Goals
- Co-locate static assets with ViewComponent classes in `src/main/java`
- Provide a simple, intuitive syntax for referencing local resources (`view:src="filename.jpg"`)
- Maintain security by restricting which file types can be served
- Support all template engines: Thymeleaf, JTE, and KTE
- Enable build systems (Maven/Gradle) to package resources from `src/main/java` correctly

### 1.3 Non-Goals
- Serving arbitrary files from the classpath (security risk)
- Complex resource transformations or bundling
- Support for resources outside the component's package

## 2. Current State

### 2.1 Existing Architecture
- ViewComponents are Java/Kotlin classes annotated with `@ViewComponent`
- Templates (`.html`, `.jte`, `.kte`) are placed in the same package as the component class
- Build configuration already includes templates from `src/main/java` in the classpath
- No mechanism exists for serving or referencing other static assets from component packages

### 2.2 Limitations
- Static assets must be placed in `src/main/resources/static` or similar Spring Boot static locations
- No logical grouping of assets with their related components
- Manual path construction required for component-specific assets

## 3. Proposed Solution

### 3.1 User Experience

#### File Structure
```
src/main/java/
  de/tschuehly/example/index/
    IndexViewComponent.java
    IndexViewComponent.html
    cat0.jpg
    styles.css
    script.js
```

#### Template Syntax (Thymeleaf)
```html
<img view:src="cat0.jpg">
<link view:href="styles.css" rel="stylesheet">
<script view:src="script.js"></script>
```

#### Rendered Output
```html
<img src="/view-src/de/tschuehly/example/index/cat0.jpg">
<link href="/view-src/de/tschuehly/example/index/styles.css" rel="stylesheet">
<script src="/view-src/de/tschuehly/example/index/script.js"></script>
```

### 3.2 URL Pattern
All component resources are served under the `/view-src/` path prefix:
- Format: `/view-src/{package-path}/{filename}`
- Example: `/view-src/de/tschuehly/example/index/cat0.jpg`

## 4. Technical Design

### 4.1 Components

#### 4.1.1 Resource Handler (Core Module)
**File**: `core/src/main/kotlin/de/tschuehly/spring/viewcomponent/core/component/ViewComponentMvcConfigurer.kt`

**Responsibilities**:
- Register resource handlers for each ViewComponent package
- Map `/view-src/{package-path}/**` to `classpath:/{package-path}/`
- Implement security filtering to allow only specific file extensions
- Automatically discover all `@ViewComponent` beans at application startup
- **Optionally restrict to specific packages** (e.g., only `*.web.*` packages)

**Implementation Details**:
```kotlin
@Component
class ViewComponentMvcConfigurer(
    private val methodReturnValueHandlers: List<HandlerMethodReturnValueHandler>,
    private val applicationContext: ApplicationContext,
    @Value("\${spring.view-component.resources.package-filter:}")
    private val packageFilter: String = ""
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val viewComponentBeans = applicationContext.getBeansWithAnnotation(ViewComponent::class.java)

        viewComponentBeans
            .map { (_, viewComponent) ->
                viewComponent.javaClass.`package`.name
            }
            .filter { packageName ->
                // Apply package filter if configured
                packageFilter.isEmpty() || packageName.contains(packageFilter)
            }
            .map { it.replace(".", "/") }
            .toSet()
            .forEach { path ->
                registry.addResourceHandler("/view-src/$path/**")
                    .addResourceLocations("classpath:/$path/")
                    .resourceChain(true)
                    .addResolver(object : PathResourceResolver() {
                        override fun getResource(resourcePath: String, location: Resource): Resource? {
                            // Only serve allowed extensions
                            if (resourcePath.matches(ALLOWED_EXTENSIONS_REGEX)) {
                                return super.getResource(resourcePath, location)
                            }
                            return null
                        }
                    })
            }

        super.addResourceHandlers(registry)
    }

    companion object {
        private val ALLOWED_EXTENSIONS_REGEX =
            ".*\\.(jpg|jpeg|png|gif|svg|webp|css|js|woff|woff2|ttf|eot|ico)$".toRegex()
    }
}
```

**Security Considerations**:
- Whitelist allowed file extensions: `.jpg`, `.jpeg`, `.png`, `.gif`, `.svg`, `.webp`, `.css`, `.js`, `.woff`, `.woff2`, `.ttf`, `.eot`, `.ico`
- Explicitly exclude: `.java`, `.class`, `.jar`, `.properties`, `.xml`, `.yml`, `.yaml`, `.html`, `.jte`, `.kte`
- Use `PathResourceResolver` to prevent path traversal attacks
- Resources only served from packages containing `@ViewComponent` beans
- **Package filter restricts resource serving to specific package patterns** (e.g., only `*.web.*` packages)

#### 4.1.2 Thymeleaf Attribute Processor
**File**: `thymeleaf/src/main/kotlin/de/tschuehly/spring/viewcomponent/thymeleaf/ThymeleafViewComponentSrcAttributeProcessor.kt`

**Responsibilities**:
- Process `view:src` attribute in Thymeleaf templates
- Resolve relative resource path to absolute `/view-src/` URL
- Support multiple attributes: `view:src`, `view:href`

**Implementation Details**:
- Extends `AbstractAttributeTagProcessor`
- Processes attributes prefixed with `view:` namespace
- Extracts template path from `ITemplateContext.templateData.template`
- Constructs full resource path: `/view-src/{template-dir}/{filename}`
- Replaces custom attribute with standard HTML attribute

**Template Resolution**:
```
Template: de/tschuehly/example/index/IndexViewComponent.html
Attribute: view:src="cat0.jpg"
Resolved: /view-src/de/tschuehly/example/index/cat0.jpg
```

#### 4.1.3 Thymeleaf Dialect Registration
**File**: `thymeleaf/src/main/kotlin/de/tschuehly/spring/viewcomponent/thymeleaf/ThymeleafViewComponentDialect.kt`

**Changes**:
- Add `ThymeleafViewComponentSrcAttributeProcessor` to processor set
- Add `ThymeleafViewComponentHrefAttributeProcessor` to processor set (for `<link>` tags)

### 4.2 JTE/KTE Support

#### 4.2.1 Approach Options

**Option A: Helper Function (Recommended)**
Provide a static helper function that JTE/KTE templates can call:

```java
// In JTE template
@import static de.tschuehly.spring.viewcomponent.jte.ViewComponentResources.src

<img src="${src(\"cat0.jpg\")}">
```

**Pros**:
- No template engine modification required
- Type-safe and IDE-friendly
- Consistent with JTE's philosophy

**Cons**:
- Slightly more verbose than attribute syntax
- Need to import helper function

**Option B: Custom Interceptor/Plugin**
Implement JTE TemplateOutput interceptor to process custom syntax.

**Pros**:
- Could support more natural syntax

**Cons**:
- More complex implementation
- Less type-safe
- May complicate template compilation

**Decision**: Start with Option A for JTE/KTE support.

#### 4.2.2 Implementation

**File**: `jte/src/main/kotlin/de/tschuehly/spring/viewcomponent/jte/ViewComponentResources.kt`

```kotlin
object ViewComponentResources {
    @JvmStatic
    fun src(filename: String): String {
        val stackTrace = Thread.currentThread().stackTrace
        // Find the calling template class
        val templateClass = findTemplateClass(stackTrace)
        val packagePath = templateClass.`package`.name.replace(".", "/")
        return "/view-src/$packagePath/$filename"
    }

    @JvmStatic
    fun href(filename: String): String = src(filename)
}
```

**Usage in JTE**:
```jte
@import static de.tschuehly.spring.viewcomponent.jte.ViewComponentResources.*

<img src="${src(\"cat0.jpg\")}">
<link href="${href(\"styles.css\")}" rel="stylesheet">
```

**Usage in KTE**:
```kte
@import de.tschuehly.spring.viewcomponent.kte.ViewComponentResources.src
@import de.tschuehly.spring.viewcomponent.kte.ViewComponentResources.href

<img src="${src(\"cat0.jpg\")}">
<link href="${href(\"styles.css\")}" rel="stylesheet">
```

### 4.3 Build Configuration

Build systems need to copy static resources from `src/main/java` (or `src/main/kotlin`) to the classpath output.

**Security Note**: Use **explicit includes** (whitelist) rather than excludes (blacklist) for better security. This ensures only approved file types are packaged, preventing accidental inclusion of sensitive files.

#### 4.3.1 Gradle (Recommended Approach)
Use the `processResources` task with explicit `include` patterns:

```kotlin
tasks.named<ProcessResources>("processResources") {
    from("src/main/java") {
        include("**/*.html")      // Templates
        include("**/*.jte")
        include("**/*.kte")
        include("**/*.jpg")       // Images
        include("**/*.jpeg")
        include("**/*.png")
        include("**/*.gif")
        include("**/*.svg")
        include("**/*.webp")
        include("**/*.css")       // Stylesheets
        include("**/*.js")        // JavaScript
        include("**/*.woff")      // Fonts
        include("**/*.woff2")
        include("**/*.ttf")
        include("**/*.eot")
        include("**/*.ico")       // Icons
    }
}
```

**Alternative (Less Secure)**: Using `sourceSets` with excludes:
```kotlin
sourceSets {
    main {
        resources {
            srcDir("src/main/java")
            exclude("**/*.java", "**/*.kt", "**/*.class")
            // Must remember to exclude ALL sensitive file types
        }
    }
}
```

#### 4.3.2 Maven
Use explicit `includes` in resource configuration:

```xml
<build>
    <resources>
        <resource>
            <directory>src/main/java</directory>
            <includes>
                <!-- Templates -->
                <include>**/*.html</include>
                <include>**/*.jte</include>
                <include>**/*.kte</include>
                <!-- Images -->
                <include>**/*.jpg</include>
                <include>**/*.jpeg</include>
                <include>**/*.png</include>
                <include>**/*.gif</include>
                <include>**/*.svg</include>
                <include>**/*.webp</include>
                <!-- Stylesheets and Scripts -->
                <include>**/*.css</include>
                <include>**/*.js</include>
                <!-- Fonts -->
                <include>**/*.woff</include>
                <include>**/*.woff2</include>
                <include>**/*.ttf</include>
                <include>**/*.eot</include>
                <!-- Icons -->
                <include>**/*.ico</include>
            </includes>
        </resource>
        <resource>
            <directory>src/main/resources</directory>
        </resource>
    </resources>
</build>
```

## 5. Security Considerations

### 5.1 Defense in Depth Strategy
Security is implemented at **two layers**:

1. **Build-time**: Only approved file types are packaged (via `processResources` includes)
2. **Runtime**: Only approved file types are served (via `PathResourceResolver` filtering)

This dual-layer approach ensures security even if one layer is misconfigured.

### 5.2 Build-Time Security (Recommended)
**Use whitelisting** via explicit `include` patterns in build configuration:

**Why whitelisting is more secure**:
- ✅ Default deny: Nothing is included unless explicitly allowed
- ✅ Fail-safe: New sensitive file types are automatically excluded
- ✅ Explicit: Clear what resources are packaged

**Why blacklisting is less secure**:
- ❌ Default allow: Everything is included unless explicitly excluded
- ❌ Fragile: Must remember to exclude every sensitive file type
- ❌ Risk: New file types (`.env`, `.key`, etc.) may be accidentally included

### 5.3 Runtime File Extension Whitelist
Only allow specific file extensions to prevent:
- Source code disclosure (`.java`, `.kt`, `.class`)
- Configuration leakage (`.properties`, `.yml`, `.xml`, `.env`)
- Template source exposure (`.html`, `.jte`, `.kte`)
- Compiled bytecode (`.class`, `.jar`)

**Allowed Extensions**:
```kotlin
private val ALLOWED_EXTENSIONS_REGEX =
    ".*\\.(jpg|jpeg|png|gif|svg|webp|css|js|woff|woff2|ttf|eot|ico)$".toRegex()
```

**Explicitly Denied** (even if present in classpath):
- Source code: `.java`, `.kt`, `.scala`, `.groovy`
- Compiled: `.class`, `.jar`, `.war`
- Templates: `.html`, `.jte`, `.kte` (should be processed, not served raw)
- Configuration: `.properties`, `.yml`, `.yaml`, `.xml`, `.json`, `.env`

### 5.4 Path Traversal Prevention
- Use Spring's `PathResourceResolver` which handles `../` and absolute paths
- Only serve resources from registered ViewComponent packages
- No custom path resolution logic
- Spring automatically normalizes and validates paths

### 5.5 Package Restriction
- Resources only served from packages containing `@ViewComponent` beans
- Dynamically registered based on actual components, not static configuration
- No wildcard or catch-all patterns
- Each component package is isolated (cannot access other package resources)
- **Optional package filter** restricts resource serving to specific package patterns

**Recommended**: Use package filter to limit resource serving to public-facing components only:

```properties
# Only serve resources from components in *.web.* packages
spring.view-component.resources.package-filter=.web.
```

**Examples**:
- `package-filter=.web.` → Only `de.example.web.*` packages
- `package-filter=.ui.` → Only `de.example.ui.*` packages
- Empty/not set → All `@ViewComponent` packages (less secure)

## 6. Configuration

### 6.1 Application Properties

#### 6.1.1 Package Filter (Recommended for Security)
**Property**: `spring.view-component.resources.package-filter`

Restricts resource serving to ViewComponents in packages matching the filter pattern.

```properties
# Recommended: Only serve resources from web-facing components
spring.view-component.resources.package-filter=.web.
```

**Examples**:
```properties
# Only components in *.web.* packages
spring.view-component.resources.package-filter=.web.

# Only components in *.ui.* packages
spring.view-component.resources.package-filter=.ui.

# Multiple patterns not supported - choose most specific
# Omit property to allow all @ViewComponent packages (less secure)
```

**Security Recommendation**: Always set this property in production to limit exposure to only public-facing components.

#### 6.1.2 Future Enhancements
Consider adding in future versions:

```properties
# Enable/disable relative resource resolution
spring.view-component.resources.enabled=true

# Custom allowed extensions (comma-separated)
spring.view-component.resources.allowed-extensions=jpg,png,css,js

# Custom path prefix (default: /view-src)
spring.view-component.resources.path-prefix=/view-src
```

### 6.2 Default Behavior
- Feature enabled by default when dependency is present
- No package filter by default (all `@ViewComponent` packages allowed)
- Standard file extensions whitelisted (jpg, png, css, js, fonts)
- Resources served under `/view-src/` path prefix

## 7. Testing Strategy

### 7.1 Unit Tests

#### Resource Handler Tests
- Test resource handler registration for multiple ViewComponents
- Test allowed extension filtering
- Test denied extension blocking
- Test path traversal prevention
- Test package isolation
- **Test package filter configuration**:
  - Test filter allows matching packages (e.g., `*.web.*`)
  - Test filter blocks non-matching packages (e.g., `*.internal.*`)
  - Test empty filter allows all packages
  - Test filter works with nested packages

#### Thymeleaf Processor Tests
- Test `view:src` attribute processing
- Test `view:href` attribute processing
- Test path resolution for nested packages
- Test attribute replacement
- Test error handling

### 7.2 Integration Tests

#### Thymeleaf Integration
- Create test ViewComponent with image resource
- Render template with `view:src` attribute
- Verify rendered HTML contains correct `/view-src/` path
- Make HTTP request to resource URL
- Verify resource is served correctly

#### JTE Integration
- Create test ViewComponent with resource
- Use helper function in JTE template
- Verify path generation
- Verify resource serving

#### Security Tests
- Attempt to access `.java` file - should return 404
- Attempt to access `.class` file - should return 404
- Attempt path traversal `../../../` - should be blocked
- Attempt to access resource from non-ViewComponent package - should return 404
- **Package filter security tests**:
  - With `package-filter=.web.`, attempt to access resource from `*.internal.*` component - should return 404
  - With `package-filter=.web.`, verify resource from `*.web.*` component - should return 200
  - Verify filter is case-sensitive and respects exact package naming

### 7.3 Example Applications
Update example applications to demonstrate feature:
- Add image resources to `IndexViewComponent`
- Add CSS file to component
- Show usage in templates

## 8. Documentation

### 8.1 README Updates
Add new section "Component Resources" explaining:
- How to place resources with components
- Syntax for Thymeleaf (`view:src`)
- Syntax for JTE/KTE (helper functions)
- Build configuration requirements
- **Security implications and best practices**:
  - Use `processResources` with explicit includes
  - Configure `package-filter` to restrict to `*.web.*` packages
  - File extension whitelisting
- Configuration options and examples

### 8.2 Example Code
Provide complete examples for:
- Thymeleaf component with image and CSS
- JTE component with resources
- KTE component with resources
- Build configuration for Gradle and Maven

### 8.3 Migration Guide
For users who want to adopt this feature:

1. **Update build configuration** (Security-first approach):
   - **Gradle**: Use `processResources` task with explicit `include` patterns
   - **Maven**: Use `<includes>` in resource configuration
   - ⚠️ **Do NOT use excludes** - use explicit includes for better security

2. **Move component-specific assets** to component packages:
   ```
   de/tschuehly/example/index/
     IndexViewComponent.java
     IndexViewComponent.html
     logo.png          ← Move here
     styles.css        ← Move here
   ```

3. **Update template syntax**:
   - **Thymeleaf**: Change `src="/static/logo.png"` to `view:src="logo.png"`
   - **JTE/KTE**: Import helper and use `src="${src(\"logo.png\")}"`

4. **Test resource loading**:
   - Start application
   - Verify resources load at `/view-src/{package-path}/{filename}`
   - Check browser console for 404 errors

5. **Clean up**:
   - Remove component-specific assets from `src/main/resources/static`
   - Update any hardcoded paths in templates

## 9. Implementation Plan

### 9.1 Phase 1: Core Infrastructure
1. Implement resource handler in `ViewComponentMvcConfigurer`
2. Add security filtering with extension whitelist
3. Add unit tests for resource handler
4. Add integration tests for resource serving

### 9.2 Phase 2: Thymeleaf Support
1. Implement `ThymeleafViewComponentSrcAttributeProcessor`
2. Implement `ThymeleafViewComponentHrefAttributeProcessor`
3. Register processors in `ThymeleafViewComponentDialect`
4. Add unit tests for attribute processors
5. Add integration tests for Thymeleaf rendering
6. Update Thymeleaf example application

### 9.3 Phase 3: JTE/KTE Support
1. Implement `ViewComponentResources` helper for JTE
2. Implement `ViewComponentResources` helper for KTE
3. Add unit tests for helpers
4. Add integration tests for JTE/KTE
5. Update JTE and KTE example applications

### 9.4 Phase 4: Documentation
1. Update README with feature documentation
2. Add code examples to documentation
3. Update build configuration examples
4. Create migration guide

### 9.5 Phase 5: Release
1. Code review
2. Performance testing
3. Security review
4. Version bump
5. Release notes
6. Publish to Maven Central

## 10. Open Questions

### 10.1 Caching Strategy
- Should resources be cached by Spring's resource chain?
- What cache headers should be set?
- How to handle cache busting in development mode?

**Recommendation**: Use Spring's default resource caching with resource chain. In development mode with DevTools, caching is automatically disabled.

### 10.2 Resource Versioning
- Should we support resource versioning/fingerprinting?
- Would users expect `/view-src/path/image.jpg?v=hash` support?

**Recommendation**: Not for initial implementation. Can be added as enhancement if needed.

### 10.3 Multiple Resources with Same Name
- What if two components have `logo.png` in different packages?
- How to avoid conflicts?

**Recommendation**: Each component's resources are served from their own package path, so conflicts are naturally avoided by package structure.

### 10.4 Nested Components
- If Component A includes Component B, how are B's resources referenced?
- Should there be a way to reference resources from other components?

**Recommendation**: Each component references its own resources. If shared resources are needed, use traditional Spring Boot static resources or extract to a shared component.

### 10.5 Build Tool Integration
- Should there be Gradle/Maven plugins to validate resource references?
- Should build fail if `view:src` references non-existent resource?

**Recommendation**: Not for initial implementation. Templates are validated at runtime. Build-time validation could be future enhancement.

## 11. Risks and Mitigations

### 11.1 Risk: Security Vulnerabilities
**Mitigation**: Strict extension whitelist, use Spring's `PathResourceResolver`, comprehensive security testing

### 11.2 Risk: Performance Impact
**Mitigation**: Leverage Spring's resource chain caching, resource handlers registered once at startup

### 11.3 Risk: Build Configuration Complexity
**Mitigation**: Provide clear documentation and working examples for both Gradle and Maven

### 11.4 Risk: Breaking Changes
**Mitigation**: Feature is additive, doesn't change existing behavior. Backward compatible.

### 11.5 Risk: Template Engine Compatibility
**Mitigation**: Test with all supported template engines (Thymeleaf, JTE, KTE), provide engine-specific implementations

## 12. Success Criteria

### 12.1 Functional Requirements
- ✅ Resources can be placed in component packages
- ✅ Resources can be referenced using simple syntax
- ✅ Resources are served correctly via HTTP
- ✅ Security filtering prevents unauthorized access
- ✅ Works with Thymeleaf, JTE, and KTE

### 12.2 Non-Functional Requirements
- ✅ No performance degradation (< 5ms overhead per request)
- ✅ Zero security vulnerabilities in code review
- ✅ 100% test coverage for new code
- ✅ Clear documentation with examples
- ✅ Backward compatible with existing applications

### 12.3 User Acceptance
- ✅ Developers can co-locate resources with components
- ✅ Syntax is intuitive and consistent with existing patterns
- ✅ Build configuration is straightforward
- ✅ Error messages are helpful

## 13. Future Enhancements

### 13.1 Resource Optimization
- Automatic image optimization (compression, format conversion)
- CSS/JS minification
- Resource bundling for production

### 13.2 Advanced Features
- Support for SCSS/LESS compilation
- TypeScript compilation
- Hot module replacement for resources in development

### 13.3 Developer Experience
- IDE plugin for resource reference validation
- Build-time checks for broken resource references
- Resource usage reporting

### 13.4 Configuration
- Customizable allowed extensions via properties
- Custom path prefix configuration
- Per-component resource configuration

## 14. References

### 14.1 Related Projects
- Rails ViewComponent: https://viewcomponent.org/
- Spring Boot Static Resources: https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content

### 14.2 PoC Implementation
- Branch: `relative-resources`
- Key files:
  - `core/src/main/kotlin/de/tschuehly/spring/viewcomponent/core/component/ViewComponentMvcConfigurer.kt`
  - `thymeleaf/src/main/kotlin/de/tschuehly/spring/viewcomponent/thymeleaf/ThymeleafViewComponentSrcAttributeProcessor.kt`
  - `examples/thymeleaf-java-example/src/main/java/de/tschuehly/example/thymeleafjava/web/index/`

### 14.3 Spring Documentation
- ResourceHandlerRegistry: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/ResourceHandlerRegistry.html
- PathResourceResolver: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/resource/PathResourceResolver.html
- Thymeleaf Dialects: https://www.thymeleaf.org/doc/tutorials/3.1/extendingthymeleaf.html

---

## Appendix A: Code Examples

### A.1 Complete Thymeleaf Example

**IndexViewComponent.java**:
```java
package de.tschuehly.example.index;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;

@ViewComponent
public class IndexViewComponent {
    public ViewContext render() {
        return new IndexView("Welcome!");
    }

    public record IndexView(String message) implements ViewContext {}
}
```

**IndexViewComponent.html**:
```html
<!DOCTYPE html>
<html>
<head>
    <link view:href="styles.css" rel="stylesheet">
    <script view:src="script.js"></script>
</head>
<body>
    <div>
        <img view:src="logo.png" alt="Logo">
        <h1 th:text="${indexView.message()}"></h1>
    </div>
</body>
</html>
```

**File Structure**:
```
de/tschuehly/example/index/
  IndexViewComponent.java
  IndexViewComponent.html
  logo.png
  styles.css
  script.js
```

### A.2 Complete JTE Example

**IndexViewComponent.java**:
```java
package de.tschuehly.example.index;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.jte.ViewContext;

@ViewComponent
public class IndexViewComponent {
    public ViewContext render() {
        return new IndexView("Welcome!");
    }

    public record IndexView(String message) implements ViewContext {}
}
```

**IndexViewComponent.jte**:
```jte
@import static de.tschuehly.spring.viewcomponent.jte.ViewComponentResources.*
@param de.tschuehly.example.index.IndexViewComponent.IndexView indexView

<!DOCTYPE html>
<html>
<head>
    <link href="${href(\"styles.css\")}" rel="stylesheet">
    <script src="${src(\"script.js\")}"></script>
</head>
<body>
    <div>
        <img src="${src(\"logo.png\")}" alt="Logo">
        <h1>${indexView.message()}</h1>
    </div>
</body>
</html>
```

### A.3 Build Configuration

**Gradle (build.gradle.kts)** - Recommended approach using `processResources`:
```kotlin
dependencies {
    implementation("de.tschuehly:spring-view-component-thymeleaf:0.10.0")
}

tasks.named<ProcessResources>("processResources") {
    from("src/main/java") {
        include("**/*.html")      // Templates
        include("**/*.jpg")       // Images
        include("**/*.jpeg")
        include("**/*.png")
        include("**/*.gif")
        include("**/*.svg")
        include("**/*.webp")
        include("**/*.css")       // Stylesheets
        include("**/*.js")        // JavaScript
        include("**/*.woff")      // Fonts
        include("**/*.woff2")
        include("**/*.ttf")
        include("**/*.eot")
        include("**/*.ico")       // Icons
    }
}
```

**Maven (pom.xml)** - Using explicit includes:
```xml
<dependencies>
    <dependency>
        <groupId>de.tschuehly</groupId>
        <artifactId>spring-view-component-thymeleaf</artifactId>
        <version>0.10.0</version>
    </dependency>
</dependencies>

<build>
    <resources>
        <resource>
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.html</include>
                <include>**/*.jpg</include>
                <include>**/*.jpeg</include>
                <include>**/*.png</include>
                <include>**/*.gif</include>
                <include>**/*.svg</include>
                <include>**/*.webp</include>
                <include>**/*.css</include>
                <include>**/*.js</include>
                <include>**/*.woff</include>
                <include>**/*.woff2</include>
                <include>**/*.ttf</include>
                <include>**/*.eot</include>
                <include>**/*.ico</include>
            </includes>
        </resource>
        <resource>
            <directory>src/main/resources</directory>
        </resource>
    </resources>
</build>
```

### A.4 Security Configuration (application.properties)

**Recommended Production Configuration**:
```properties
# Restrict resource serving to only web-facing ViewComponents
spring.view-component.resources.package-filter=.web.
```

**Example Package Structure**:
```
com/example/myapp/
  web/                          ← Resources ALLOWED (public-facing)
    index/IndexViewComponent
    profile/ProfileViewComponent
  internal/                     ← Resources BLOCKED (internal components)
    admin/AdminViewComponent
    report/ReportViewComponent
```

**Result with `package-filter=.web.`**:
- ✅ `/view-src/com/example/myapp/web/index/logo.png` → **200 OK**
- ✅ `/view-src/com/example/myapp/web/profile/avatar.jpg` → **200 OK**
- ❌ `/view-src/com/example/myapp/internal/admin/chart.png` → **404 Not Found**
- ❌ `/view-src/com/example/myapp/internal/report/data.csv` → **404 Not Found**

**Development vs Production**:
```properties
# application-dev.properties (more permissive for development)
# spring.view-component.resources.package-filter= (empty = all packages)

# application-prod.properties (strict for production)
spring.view-component.resources.package-filter=.web.
```
