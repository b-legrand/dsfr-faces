package fr.gouv.dsfr.faces.render;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Base of the single-control fields (textarea, date, select):
 * <ul>
 *     <li><b>with a {@code label} attribute</b>: complete DSFR group, the wrapper carrying the client id
 *     (as {@code dsfr:inputText}): {@code <div id="form-x" class="fr-input-group">label, control
 *     id="form-x-input", messages</div>};</li>
 *     <li><b>without label</b>: the bare control carrying the client id ({@code id="form-x"}), so that an
 *     existing {@code h:outputLabel for="x"}, {@code h:message for="x"} or {@code dsfr:field} keeps working
 *     (pages migrated from RichFaces).</li>
 * </ul>
 */
public abstract class DsfrFieldRendererBase extends DsfrInputRendererBase {

    protected static final Map<String, String> DOM_EVENTS = Map.of("valueChange", "change");

    /** Group kind: input (fr-input-group) or select (fr-select-group). */
    protected abstract String groupKind();

    /** Writes the control (textarea, input, select). */
    protected abstract void encodeControl(FacesContext context, UIInput input, String controlId, String messagesId,
                                          String state) throws IOException;

    @Override
    protected boolean isNotSubmittable(UIInput input) {
        return bool(input, "disabled") || bool(input, "readonly");
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        UIInput input = (UIInput) component;
        String id = input.getClientId(context);
        String label = str(input, "label");
        List<FacesMessage> messages = context.getMessageList(id);
        String state = groupState(input, messages);
        if (isBlank(label)) {
            encodeControl(context, input, id, null, state);
            return;
        }
        String group = "fr-" + groupKind() + "-group";
        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes(group, state == null ? null : group + "--" + state,
                bool(input, "disabled") ? group + "--disabled" : null), null);
        writeLabel(w, id + "-input", label, str(input, "hint"), input.isRequired());
        encodeControl(context, input, id + "-input", id + "-messages", state);
        writeMessagesGroup(w, null, id + "-messages", null, messages);
        w.endElement("div");
    }

    /** id, name (the client id), class, ARIA, disabled / readonly and client behaviors of a control. */
    protected static void writeControlAttributes(FacesContext context, UIInput input, String controlId,
                                                 String messagesId, String state, String cssClass) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.writeAttribute("id", controlId, null);
        w.writeAttribute("name", input.getClientId(context), null);
        w.writeAttribute("class", classes(cssClass, str(input, "styleClass")), null);
        attr(w, "aria-describedby", messagesId);
        if ("error".equals(state)) {
            w.writeAttribute("aria-invalid", "true", null);
        }
        if (input.isRequired()) {
            w.writeAttribute("aria-required", "true", null);
        }
        if (bool(input, "disabled")) {
            w.writeAttribute("disabled", "disabled", null);
        }
        if (bool(input, "readonly")) {
            w.writeAttribute("readonly", "readonly", null);
        }
        attr(w, "title", str(input, "title"));
        // the source of ajax requests is the client id: the wrapper in group mode, the control otherwise
        renderBehaviors(context, input, input.getClientId(context), DOM_EVENTS, false);
    }

    /** String form of an item value, with the converter of the component or the by-type converter. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static String itemString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String s) {
            return s;
        }
        Converter converter = component instanceof UIInput input ? input.getConverter() : null;
        if (converter == null) {
            converter = context.getApplication().createConverter(value.getClass());
        }
        return converter == null ? value.toString() : converter.getAsString(context, component, value);
    }
}
