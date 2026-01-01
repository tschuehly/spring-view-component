@import de.tschuehly.example.fragments.ButtonComponent.*

<%--
  ButtonComponent JTE template with type-based conditional rendering.

  JTE natively supports instanceof checks with pattern matching,
  providing compile-time type safety.
--%>

@if(model instanceof PrimaryButton primaryButton)
    <button class="btn btn-primary"
            type="button"
            data-action="${primaryButton.action()}">
        ${primaryButton.label()}
    </button>
@elseif(model instanceof SecondaryButton secondaryButton)
    <button class="btn btn-secondary"
            type="button"
            data-action="${secondaryButton.action()}">
        ${secondaryButton.label()}
    </button>
@elseif(model instanceof DangerButton dangerButton)
    <button class="btn btn-danger"
            type="button"
            data-action="${dangerButton.action()}"
            onclick="return confirm('${dangerButton.confirmMessage()}')">
        ${dangerButton.label()}
    </button>
@else
    <%-- Fallback for unknown context types --%>
    <div class="error">Unknown button type: ${model.getClass().getSimpleName()}</div>
@endif
