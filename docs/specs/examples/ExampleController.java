package de.tschuehly.example.fragments;

import de.tschuehly.spring.viewcomponent.core.IViewContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Example controller demonstrating fragment rendering usage.
 *
 * Different endpoints return different ViewContext types from the same ViewComponent,
 * causing different fragments to be rendered.
 */
@Controller
public class ExampleController {

    private final ButtonComponent buttonComponent;

    public ExampleController(ButtonComponent buttonComponent) {
        this.buttonComponent = buttonComponent;
    }

    /**
     * Returns a primary button - renders the PrimaryButton fragment
     */
    @GetMapping("/button/submit")
    IViewContext submitButton() {
        return buttonComponent.primary("Submit Form", "/api/submit");
    }

    /**
     * Returns a secondary button - renders the SecondaryButton fragment
     */
    @GetMapping("/button/cancel")
    IViewContext cancelButton() {
        return buttonComponent.secondary("Cancel", "/api/cancel");
    }

    /**
     * Returns a danger button - renders the DangerButton fragment
     */
    @GetMapping("/button/delete")
    IViewContext deleteButton() {
        return buttonComponent.danger(
            "Delete Account",
            "/api/delete-account",
            "Are you sure you want to delete your account? This action cannot be undone."
        );
    }

    /**
     * Example showing nested component rendering with fragments.
     *
     * The page component can receive different button variants
     * as child components.
     */
    @GetMapping("/page/submit-form")
    IViewContext submitFormPage() {
        var submitButton = buttonComponent.primary("Submit", "/submit");
        var cancelButton = buttonComponent.secondary("Cancel", "/cancel");

        // In a real application, you might have a PageComponent that accepts
        // button components as parameters
        // return pageComponent.render(submitButton, cancelButton);

        return submitButton; // Simplified for this example
    }
}
