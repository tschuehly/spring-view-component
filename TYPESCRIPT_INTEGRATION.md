# TypeScript Integration with Bun

This guide explains how to integrate TypeScript with Spring ViewComponent using Bun for fast, modern JavaScript compilation.

## Overview

The TypeScript integration allows you to add client-side behavior to your view components by:
- Writing TypeScript files alongside your component classes
- Automatically compiling them to JavaScript using Bun
- Including the compiled scripts in your templates with a simple method call

## Architecture

### Convention-Based Structure

TypeScript files follow the same convention as templates:

```
com/example/web/simple/
  ├── SimpleViewComponent.kt       # Server-side component
  ├── SimpleViewComponent.html     # Thymeleaf template
  └── SimpleViewComponent.ts       # Client-side behavior (TypeScript)
```

### Build Pipeline

1. **TypeScript files** are placed next to component classes in `src/main/kotlin/` or `src/main/java/`
2. **Bun compiles** TypeScript → JavaScript during the build process
3. **Compiled JavaScript** is output to `src/main/resources/static/js/components/`
4. **Templates include** the script using `${componentView.getScriptPath()}`

The compiled JavaScript path follows this pattern:
```
/js/components/{package-path}/{ComponentName}.js
```

Example:
- TypeScript: `src/main/kotlin/com/example/web/simple/SimpleViewComponent.ts`
- Compiled: `src/main/resources/static/js/components/com/example/web/simple/SimpleViewComponent.js`
- URL: `/js/components/com/example/web/simple/SimpleViewComponent.js`

## Setup

### 1. Install Bun

Bun must be installed on your system. Visit [bun.sh](https://bun.sh) for installation instructions.

```bash
# Verify Bun is installed
bun --version
```

### 2. Add Configuration Files

Create these files in your project root:

**package.json**
```json
{
  "name": "your-project",
  "version": "0.0.1",
  "scripts": {
    "build": "bun run build.ts",
    "watch": "bun run build.ts --watch"
  },
  "dependencies": {
    "glob": "^11.0.0"
  },
  "devDependencies": {
    "@types/bun": "latest"
  }
}
```

**tsconfig.json**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "lib": ["ES2020", "DOM"],
    "moduleResolution": "bundler",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "outDir": "./src/main/resources/static/js/components",
    "rootDir": "./src/main/kotlin",
    "declaration": true,
    "declarationMap": true,
    "sourceMap": true,
    "allowImportingTsExtensions": true,
    "noEmit": true
  },
  "include": [
    "src/main/kotlin/**/*.ts",
    "src/main/java/**/*.ts"
  ],
  "exclude": [
    "node_modules",
    "src/main/resources"
  ]
}
```

**build.ts**

See the full build script in the example project: `examples/thymeleaf-kotlin-example/build.ts`

### 3. Update build.gradle.kts

Add these tasks to integrate Bun with your Gradle build:

```kotlin
// TypeScript compilation with Bun
tasks.register<Exec>("bunInstall") {
    group = "build"
    description = "Install Bun dependencies"
    commandLine("bun", "install")
    inputs.file("package.json")
    outputs.dir("node_modules")
    onlyIf { file("package.json").exists() }
}

tasks.register<Exec>("compileFrontend") {
    group = "build"
    description = "Compile TypeScript files to JavaScript using Bun"
    dependsOn("bunInstall")
    commandLine("bun", "run", "build")
    inputs.files(fileTree("src/main/kotlin").matching { include("**/*.ts") })
    inputs.files(fileTree("src/main/java").matching { include("**/*.ts") })
    outputs.dir("src/main/resources/static/js/components")
    onlyIf {
        fileTree("src/main/kotlin").matching { include("**/*.ts") }.files.isNotEmpty() ||
        fileTree("src/main/java").matching { include("**/*.ts") }.files.isNotEmpty()
    }
}

tasks.named("processResources") {
    dependsOn("compileFrontend")
}

tasks.register<Exec>("watchFrontend") {
    group = "build"
    description = "Watch and compile TypeScript files on change"
    commandLine("bun", "run", "watch")
}
```

### 4. Install Dependencies

```bash
bun install
```

## Usage

### Creating a TypeScript Component

**1. Create the TypeScript file next to your component:**

```typescript
// SimpleViewComponent.ts
class SimpleViewComponent {
  private container: HTMLElement | null;

  constructor() {
    this.container = null;
    this.init();
  }

  private init(): void {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => this.setup());
    } else {
      this.setup();
    }
  }

  private setup(): void {
    console.log('SimpleViewComponent initialized!');

    this.container = document.querySelector('[data-component="simple-view"]');

    if (this.container) {
      this.attachEventHandlers();
    }
  }

  private attachEventHandlers(): void {
    this.container?.addEventListener('click', () => {
      console.log('Component clicked!');
    });
  }
}

// Initialize
new SimpleViewComponent();

export { SimpleViewComponent };
```

**2. Update your template to include the script:**

```html
<div data-component="simple-view">
  <h2>This is the SimpleViewComponent</h2>
  <!--/*@thymesVar id="simpleView" type="...SimpleView"*/-->
  <div th:text="${simpleView.helloWorld}"></div>

  <!-- Include component-specific TypeScript -->
  <script th:src="${simpleView.getScriptPath()}" type="module"></script>
</div>
```

**3. Build and run:**

```bash
# Compile TypeScript
bun run build

# Or use Gradle (which runs Bun automatically)
./gradlew build

# Watch mode for development
bun run watch
```

## API Reference

### IViewContext Methods

The `IViewContext` interface provides these methods for script resolution:

#### `getScriptPath(): String`
Returns the conventional script path for this component.

```kotlin
data class SimpleView(val message: String) : ViewContext

// In template:
<script th:src="${simpleView.getScriptPath()}"></script>
// Outputs: /js/components/com/example/web/simple/SimpleViewComponent.js
```

#### `getScripts(): List<String>`
Returns a list of all script paths for this component. Override to add additional scripts.

```kotlin
data class SimpleView(val message: String) : ViewContext {
    override fun getScripts(): List<String> {
        return listOf(
            getScriptPath(),
            "/js/vendor/chart.js"
        )
    }
}

// In template:
<script th:each="script : ${simpleView.getScripts()}"
        th:src="${script}" type="module"></script>
```

#### Companion Object Methods

- `getComponentScriptPath(context: IViewContext): String` - Get script path for any context
- `getComponentScripts(context: IViewContext): List<String>` - Get all scripts for any context

## Examples

### Example 1: Simple Interactive Component

**SimpleViewComponent.ts**
```typescript
class SimpleViewComponent {
  constructor() {
    document.addEventListener('DOMContentLoaded', () => {
      const container = document.querySelector('[data-component="simple-view"]');
      container?.addEventListener('click', () => {
        console.log('Clicked!');
      });
    });
  }
}

new SimpleViewComponent();
```

**SimpleViewComponent.html**
```html
<div data-component="simple-view">
  <p>Click me!</p>
  <script th:src="${simpleView.getScriptPath()}" type="module"></script>
</div>
```

### Example 2: Component with Keyboard Shortcuts

**IndexViewComponent.ts**
```typescript
class IndexViewComponent {
  private setupKeyboardShortcuts(): void {
    document.addEventListener('keydown', (e) => {
      if (e.key === '1') window.location.href = '/';
      if (e.key === '2') window.location.href = '/simple';
      if (e.key === '3') window.location.href = '/layout';
    });
  }

  constructor() {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => this.setupKeyboardShortcuts());
    } else {
      this.setupKeyboardShortcuts();
    }
  }
}

new IndexViewComponent();
```

### Example 3: Type-Safe Data Passing

**TypeScript Interface**
```typescript
interface SimpleViewData {
  message: string;
  count: number;
}

class SimpleViewComponent {
  private data: SimpleViewData;

  constructor(data: SimpleViewData) {
    this.data = data;
    console.log(`Message: ${data.message}, Count: ${data.count}`);
  }
}

// Pass data from template
const data = JSON.parse(document.getElementById('component-data')?.textContent ?? '{}');
new SimpleViewComponent(data);
```

**Template**
```html
<script id="component-data" type="application/json" th:inline="text">
{
  "message": "[[${simpleView.message}]]",
  "count": [[${simpleView.count}]]
}
</script>
<script th:src="${simpleView.getScriptPath()}" type="module"></script>
```

## Build Commands

### Compile TypeScript
```bash
bun run build
```

### Watch Mode (Development)
```bash
bun run watch
```

### Gradle Integration
```bash
# Build includes TypeScript compilation
./gradlew build

# Watch frontend only
./gradlew watchFrontend
```

## Project Structure

```
your-project/
├── build.ts                          # Bun build script
├── package.json                      # Bun dependencies
├── tsconfig.json                     # TypeScript config
├── build.gradle.kts                  # Gradle with Bun tasks
└── src/
    └── main/
        ├── kotlin/                   # or java/
        │   └── com/example/web/
        │       └── simple/
        │           ├── SimpleViewComponent.kt
        │           ├── SimpleViewComponent.html
        │           └── SimpleViewComponent.ts    # ← TypeScript here
        └── resources/
            └── static/
                └── js/
                    └── components/              # ← Compiled JS here
                        └── com/example/web/simple/
                            ├── SimpleViewComponent.js
                            └── SimpleViewComponent.js.map
```

## Best Practices

### 1. Use Data Attributes for Component Selection
```html
<div data-component="simple-view">
  <!-- Content -->
</div>
```

```typescript
const container = document.querySelector('[data-component="simple-view"]');
```

### 2. Handle DOM Ready State
```typescript
private init(): void {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => this.setup());
  } else {
    this.setup();
  }
}
```

### 3. Use Type Interfaces
```typescript
interface ComponentData {
  id: number;
  name: string;
}
```

### 4. Clean Up Event Listeners
```typescript
class MyComponent {
  private cleanup: (() => void)[] = [];

  private addCleanup(fn: () => void): void {
    this.cleanup.push(fn);
  }

  public destroy(): void {
    this.cleanup.forEach(fn => fn());
  }
}
```

### 5. Use Module Scripts
```html
<script th:src="${view.getScriptPath()}" type="module"></script>
```

## Troubleshooting

### Scripts not loading?
- Check the browser console for 404 errors
- Verify compiled JavaScript exists in `src/main/resources/static/js/components/`
- Ensure `compileFrontend` task ran successfully

### TypeScript compilation errors?
- Run `bun run build` directly to see detailed errors
- Check `tsconfig.json` configuration
- Verify file paths in `build.ts`

### Module not found errors?
- Run `bun install` to ensure dependencies are installed
- Check `package.json` for required dependencies

### Scripts not updating?
- Use `bun run watch` during development
- Or run `./gradlew build` to rebuild
- Clear browser cache

## Benefits

✅ **Fast Compilation** - Bun is blazingly fast compared to traditional TypeScript compilers
✅ **No Configuration** - Works out of the box with minimal setup
✅ **Convention-Based** - Scripts automatically resolve based on component location
✅ **Type Safety** - Full TypeScript support with strict type checking
✅ **Source Maps** - Generated automatically for debugging
✅ **Hot Reload** - Watch mode for rapid development
✅ **Gradle Integration** - Seamless integration with existing build process

## Advanced Topics

### Sharing Code Between Components

Create a shared utilities file:

```typescript
// src/main/resources/static/js/utils.ts
export function formatDate(date: Date): string {
  return date.toLocaleDateString();
}
```

Import in your component:

```typescript
// SimpleViewComponent.ts
import { formatDate } from '/js/utils.js';

console.log(formatDate(new Date()));
```

### Using NPM Packages

Install any NPM package:

```bash
bun add axios
```

Use in your component:

```typescript
import axios from 'axios';

async function fetchData() {
  const response = await axios.get('/api/data');
  return response.data;
}
```

### Type Generation (Future Enhancement)

Generate TypeScript interfaces from ViewContext classes:

```kotlin
// Gradle task
tasks.register("generateTypes") {
  // Scan ViewContext classes
  // Generate .d.ts files
}
```

## Migration Guide

### From Inline Scripts

**Before:**
```html
<script>
  document.querySelector('.button').addEventListener('click', () => {
    alert('Clicked!');
  });
</script>
```

**After:**
```typescript
// SimpleViewComponent.ts
class SimpleViewComponent {
  constructor() {
    document.querySelector('.button')?.addEventListener('click', () => {
      alert('Clicked!');
    });
  }
}
new SimpleViewComponent();
```

```html
<script th:src="${simpleView.getScriptPath()}" type="module"></script>
```

## See Also

- [Bun Documentation](https://bun.sh/docs)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Spring ViewComponent Documentation](README.md)
