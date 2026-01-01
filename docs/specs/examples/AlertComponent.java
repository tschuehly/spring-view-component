package de.tschuehly.example.fragments;

import de.tschuehly.spring.viewcomponent.core.component.ViewComponent;
import de.tschuehly.spring.viewcomponent.thymeleaf.ViewContext;

/**
 * Advanced example showing fragment rendering for alert messages.
 *
 * Demonstrates:
 * - Different data requirements for different contexts (e.g., ErrorAlert has stackTrace)
 * - Compile-time safety (can't create ErrorAlert without stackTrace)
 * - Single template with multiple presentation variants
 */
@ViewComponent
public class AlertComponent {

    /**
     * Informational alert - simple message display
     */
    public record InfoAlert(String message) implements ViewContext {}

    /**
     * Warning alert - message with additional details
     */
    public record WarningAlert(String message, String details) implements ViewContext {}

    /**
     * Error alert - message with stack trace
     */
    public record ErrorAlert(String message, String stackTrace) implements ViewContext {}

    /**
     * Success alert - simple message for successful operations
     */
    public record SuccessAlert(String message) implements ViewContext {}

    public InfoAlert info(String message) {
        return new InfoAlert(message);
    }

    public WarningAlert warning(String message, String details) {
        return new WarningAlert(message, details);
    }

    public ErrorAlert error(String message, String stackTrace) {
        return new ErrorAlert(message, stackTrace);
    }

    public SuccessAlert success(String message) {
        return new SuccessAlert(message);
    }

    /**
     * Convenience method to create error alerts from exceptions
     */
    public ErrorAlert error(String message, Exception exception) {
        var stackTrace = buildStackTrace(exception);
        return new ErrorAlert(message, stackTrace);
    }

    private String buildStackTrace(Exception exception) {
        var sb = new StringBuilder();
        sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage()).append("\n");

        for (var element : exception.getStackTrace()) {
            sb.append("  at ").append(element.toString()).append("\n");
        }

        if (exception.getCause() != null) {
            sb.append("Caused by: ").append(buildStackTrace((Exception) exception.getCause()));
        }

        return sb.toString();
    }
}
