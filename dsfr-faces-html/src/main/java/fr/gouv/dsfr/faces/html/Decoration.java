package fr.gouv.dsfr.faces.html;

import fr.gouv.dsfr.faces.component.DsfrField;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIMessage;
import jakarta.faces.component.html.HtmlOutputLabel;
import jakarta.faces.context.FacesContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What to add to the main element of a standard renderer, for one component.
 *
 * @param dsfrClass  DSFR class and element names
 * @param attributes attributes to add ({@code aria-describedby} is merged with an existing value)
 * @param required   label only: append the required mark
 * @param hint       label only: hint appended in the label, or {@code null}
 */
record Decoration(DsfrClass dsfrClass, Map<String, String> attributes, boolean required, String hint) {

    static Decoration of(FacesContext context, UIComponent component, DsfrClass dsfrClass) {
        Map<String, String> attributes = new LinkedHashMap<>();
        boolean required = false;
        String hint = null;
        DsfrField field = component.getParent() instanceof DsfrField f ? f : null;

        if (component instanceof UIInput input) {
            if (!input.isValid()) {
                attributes.put("aria-invalid", "true");
            }
            if (field != null && field.getInput() == input) {
                if (input.isRequired()) {
                    // not "required": browser validation must not pre-empt Faces validation and its messages
                    attributes.put("aria-required", "true");
                }
                UIMessage message = field.getMessage(input);
                if (message != null) {
                    attributes.put("aria-describedby", message.getClientId(context));
                }
            }
        } else if (component instanceof HtmlOutputLabel label && field != null && field.isLabelOfInput(label)) {
            required = field.getInput().isRequired();
            hint = field.getHint();
        }
        return new Decoration(dsfrClass, attributes, required, hint);
    }

    boolean hasLabelSuffix() {
        return required || (hint != null && !hint.isBlank());
    }
}
