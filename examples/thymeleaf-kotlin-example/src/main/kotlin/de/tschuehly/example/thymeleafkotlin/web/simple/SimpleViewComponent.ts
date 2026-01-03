/**
 * Client-side behavior for SimpleViewComponent
 *
 * This TypeScript file is automatically compiled by Bun and loaded
 * alongside the SimpleViewComponent template.
 */

interface SimpleViewComponentData {
  helloWorld: string;
}

class SimpleViewComponent {
  private container: HTMLElement | null;

  constructor() {
    this.container = null;
    this.init();
  }

  private init(): void {
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => this.setup());
    } else {
      this.setup();
    }
  }

  private setup(): void {
    console.log('SimpleViewComponent TypeScript initialized!');

    // Find the component container
    // You can add a data attribute or ID to your template for easier selection
    this.container = document.querySelector('[data-component="simple-view"]');

    if (this.container) {
      this.attachEventHandlers();
      this.enhanceComponent();
    }
  }

  private attachEventHandlers(): void {
    if (!this.container) return;

    // Example: Add click handler to the component
    this.container.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      console.log('Component clicked:', target);

      // Add a visual effect
      this.container?.classList.add('clicked');
      setTimeout(() => {
        this.container?.classList.remove('clicked');
      }, 300);
    });

    // Example: Handle input changes
    const input = this.container.querySelector('input');
    if (input) {
      input.addEventListener('input', (e) => {
        const value = (e.target as HTMLInputElement).value;
        console.log('Input changed:', value);
        this.onInputChange(value);
      });
    }
  }

  private enhanceComponent(): void {
    if (!this.container) return;

    // Example: Add dynamic content or styling
    this.container.style.transition = 'all 0.3s ease';

    // Add a data attribute to show TypeScript is working
    this.container.setAttribute('data-enhanced', 'true');

    console.log('SimpleViewComponent enhanced with TypeScript');
  }

  private onInputChange(value: string): void {
    // Example: Real-time validation or formatting
    console.log('Processing input:', value);

    // You could emit custom events, update other parts of the page, etc.
    const event = new CustomEvent('simple-view:input-change', {
      detail: { value },
      bubbles: true
    });
    this.container?.dispatchEvent(event);
  }

  /**
   * Public method to update the component from external code
   */
  public updateContent(newContent: string): void {
    if (!this.container) return;

    const contentElement = this.container.querySelector('[data-content]');
    if (contentElement) {
      contentElement.textContent = newContent;
    }
  }
}

// Initialize the component
const simpleViewComponent = new SimpleViewComponent();

// Export for external use if needed
export { SimpleViewComponent, type SimpleViewComponentData };
