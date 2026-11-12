package fr.gouv.dsfr.faces.render;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Decoding, conversion and the label / messages markup shared by DSFR form fields
 * (input, textarea, select, checkbox, radio, toggle...).
 */
public abstract class DsfrInputRendererBase extends DsfrRendererBase {

    /** Whether the field must not be submitted (disabled / readonly). */
    protected abstract boolean isNotSubmittable(UIInput input);

    @Override
    public void decode(FacesContext context, UIComponent component) {
        UIInput input = (UIInput) component;
        if (isNotSubmittable(input)) {
            return;
        }
        decodeBehaviors(context, component);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String value = params.get(component.getClientId(context));
        if (value != null) {
            input.setSubmittedValue(value);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
            throws ConverterException {
        Converter converter = converterFor(context, (UIInput) component);
        return converter == null ? submittedValue : converter.getAsObject(context, component, (String) submittedValue);
    }

    /** Explicit converter, or by-type converter based on the value expression type (Integer, LocalDate...). */
    protected static Converter<?> converterFor(FacesContext context, UIInput input) {
        Converter<?> converter = input.getConverter();
        if (converter != null) {
            return converter;
        }
        ValueExpression ve = input.getValueExpression("value");
        if (ve == null) {
            return null;
        }
        Class<?> type = ve.getType(context.getELContext());
        if (type == null || type == Object.class || type == String.class) {
            return null;
        }
        return context.getApplication().createConverter(type);
    }

    /** Value to redisplay: the submitted value after a failed validation, otherwise the converted model value. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static String currentValue(FacesContext context, UIInput input) {
        Object submitted = input.getSubmittedValue();
        if (submitted != null) {
            return submitted.toString();
        }
        Object value = input.getValue();
        if (value == null) {
            return "";
        }
        if (value instanceof String s) {
            return s;
        }
        Converter converter = input.getConverter();
        if (converter == null) {
            converter = context.getApplication().createConverter(value.getClass());
        }
        return converter == null ? value.toString() : converter.getAsString(context, input, value);
    }

    /**
     * {@code <label class="fr-label" for="..">Label *<span class="fr-hint-text">hint</span></label>}
     */
    protected static void writeLabel(ResponseWriter w, String forId, String label, String hint, boolean required)
            throws IOException {
        w.startElement("label", null);
        w.writeAttribute("class", "fr-label", null);
        w.writeAttribute("for", forId, null);
        if (label != null) {
            w.writeText(label, null);
        }
        if (required) {
            w.writeText(" *", null);
        }
        if (!isBlank(hint)) {
            w.startElement("span", null);
            w.writeAttribute("class", "fr-hint-text", null);
            w.writeText(hint, null);
            w.endElement("span");
        }
        w.endElement("label");
    }

    /**
     * {@code <div class="fr-messages-group" id=".." aria-live="polite"><p class="fr-message fr-message--error">..</p></div>}
     * <p>
     * Always rendered, even when empty: the live region must exist before its content changes.
     */
    protected static void writeMessagesGroup(ResponseWriter w, String messagesId, List<FacesMessage> messages)
            throws IOException {
        w.startElement("div", null);
        w.writeAttribute("class", "fr-messages-group", null);
        w.writeAttribute("id", messagesId, null);
        w.writeAttribute("aria-live", "polite", null);
        int i = 0;
        for (FacesMessage message : messages) {
            w.startElement("p", null);
            w.writeAttribute("class", "fr-message fr-message--" + severity(message), null);
            w.writeAttribute("id", messagesId + "-" + i++, null);
            w.writeText(text(message), null);
            w.endElement("p");
            message.rendered();
        }
        w.endElement("div");
    }

    /** DSFR group modifier: error if any error message (or invalid), info if only info messages. */
    protected static String groupState(UIInput input, List<FacesMessage> messages) {
        if (!input.isValid() || hasError(messages)) {
            return "error";
        }
        if (!messages.isEmpty() && messages.stream().allMatch(m -> "info".equals(severity(m)))) {
            return "info";
        }
        return null;
    }
}
