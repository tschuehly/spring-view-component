package de.tschuehly.example.fragments;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;

/**
 * Example ViewComponent demonstrating fragment rendering with multiple ViewContext types.
 *
 * This single component can render different button variants (primary, secondary, danger)
 * based on the ViewContext type returned from the render methods.
 */
@ViewComponent
public class ButtonComponent {

    /**
     * ViewContext for primary action buttons.
     */
    public record PrimaryButton(
        String label,
        String action
    ) implements ViewContext {}

    /**
     * ViewContext for secondary/alternative action buttons.
     */
    public record SecondaryButton(
        String label,
        String action
    ) implements ViewContext {}

    /**
     * ViewContext for dangerous/destructive action buttons.
     * Includes a confirmation message that will be shown before execution.
     */
    public record DangerButton(
        String label,
        String action,
        String confirmMessage
    ) implements ViewContext {}

    /**
     * Renders a primary button for main actions.
     */
    public PrimaryButton primary(String label, String action) {
        return new PrimaryButton(label, action);
    }

    /**
     * Renders a secondary button for alternative actions.
     */
    public SecondaryButton secondary(String label, String action) {
        return new SecondaryButton(label, action);
    }

    /**
     * Renders a danger button for destructive actions.
     */
    public DangerButton danger(String label, String action, String confirmMessage) {
        return new DangerButton(label, action, confirmMessage);
    }
}
