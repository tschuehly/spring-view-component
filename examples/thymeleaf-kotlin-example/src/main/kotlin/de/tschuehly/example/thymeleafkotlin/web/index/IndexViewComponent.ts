/**
 * Client-side behavior for IndexViewComponent
 *
 * This demonstrates a simpler TypeScript integration for a navigation page
 */

class IndexViewComponent {
  constructor() {
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
    console.log('IndexViewComponent TypeScript loaded!');

    // Enhance navigation links with smooth transitions
    this.enhanceNavigation();

    // Add keyboard shortcuts
    this.setupKeyboardShortcuts();
  }

  private enhanceNavigation(): void {
    const links = document.querySelectorAll('a');

    links.forEach((link, index) => {
      // Add visual feedback on hover
      link.addEventListener('mouseenter', () => {
        link.style.transform = 'translateX(5px)';
        link.style.transition = 'transform 0.2s ease';
      });

      link.addEventListener('mouseleave', () => {
        link.style.transform = 'translateX(0)';
      });

      // Log navigation
      link.addEventListener('click', (e) => {
        const href = link.getAttribute('href');
        console.log(`Navigating to: ${href}`);
      });
    });
  }

  private setupKeyboardShortcuts(): void {
    document.addEventListener('keydown', (e) => {
      // Press '1' for IndexViewComponent
      if (e.key === '1') {
        window.location.href = '/';
      }
      // Press '2' for SimpleViewComponent
      if (e.key === '2') {
        window.location.href = '/simple';
      }
      // Press '3' for LayoutViewComponent
      if (e.key === '3') {
        window.location.href = '/layout';
      }
    });

    console.log('Keyboard shortcuts enabled: Press 1, 2, or 3 to navigate');
  }
}

// Initialize
new IndexViewComponent();

export { IndexViewComponent };
