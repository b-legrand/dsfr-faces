package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrSelectBooleanCheckbox;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UISelectBoolean;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * DSFR checkbox:
 * <pre>{@code
 * <div class="fr-checkbox-group fr-checkbox-group--error" id="form-cgu">
 *   <input type="checkbox" id="form-cgu-input" name="form-cgu" value="true" aria-describedby="form-cgu-messages">
 *   <label class="fr-label" for="form-cgu-input">J'accepte *<span class="fr-hint-text">…</span></label>
 *   <div class="fr-messages-group" id="form-cgu-messages" aria-live="polite">…</div>
 * </div>
 * }</pre>
 * Also the base of {@code dsfr:toggle}.
 */
@FacesRenderer(componentFamily = UISelectBoolean.COMPONENT_FAMILY, rendererType = DsfrSelectBooleanCheckbox.RENDERER_TYPE)
public class DsfrSelectBooleanCheckboxRenderer extends DsfrInputRendererBase {

    protected static final Map<String, String> DOM_EVENTS = Map.of("valueChange", "change");

    @Override
    protected boolean isNotSubmittable(UIInput input) {
        return bool(input, "disabled") || bool(input, "readonly");
    }

    /** An unchecked box is not submitted: absent means false. */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        UIInput input = (UIInput) component;
        if (isNotSubmittable(input)) {
            return;
        }
        decodeBehaviors(context, component);
        String value = context.getExternalContext().getRequestParameterMap().get(component.getClientId(context));
        input.setSubmittedValue(value != null && !"false".equals(value) ? "true" : "false");
    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) {
        return Boolean.valueOf((String) submittedValue);
    }

    protected static boolean isChecked(UIInput input) {
        Object submitted = input.getSubmittedValue();
        return submitted != null ? "true".equals(submitted) : Boolean.TRUE.equals(input.getValue());
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        UIInput input = (UIInput) component;
        String id = input.getClientId(context);
        List<FacesMessage> messages = context.getMessageList(id);
        String state = groupState(input, messages);
        ResponseWriter w = context.getResponseWriter();

        w.startElement("div", null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-checkbox-group",
                "sm".equals(str(input, "size")) ? "fr-checkbox-group--sm" : null,
                state == null ? null : "fr-checkbox-group--" + state, str(input, "styleClass")), null);
        writeCheckbox(context, input, id, state, null);
        writeLabel(w, id + "-input", str(input, "label"), str(input, "hint"), input.isRequired());
        writeMessagesGroup(w, null, id + "-messages", null, messages);
        w.endElement("div");
    }

    /** {@code <input type="checkbox" id="{id}-input" name="{id}" value="true" …>} */
    protected static void writeCheckbox(FacesContext context, UIInput input, String id, String state, String cssClass)
            throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement("input", input);
        w.writeAttribute("type", "checkbox", null);
        attr(w, "class", cssClass);
        w.writeAttribute("id", id + "-input", null);
        w.writeAttribute("name", id, null);
        w.writeAttribute("value", "true", null);
        if (isChecked(input)) {
            w.writeAttribute("checked", "checked", null);
        }
        w.writeAttribute("aria-describedby", id + "-messages", null);
        if ("error".equals(state)) {
            w.writeAttribute("aria-invalid", "true", null);
        }
        if (input.isRequired()) {
            w.writeAttribute("aria-required", "true", null);
        }
        if (bool(input, "disabled")) {
            w.writeAttribute("disabled", "disabled", null);
        }
        renderBehaviors(context, input, id, DOM_EVENTS, false);
        w.endElement("input");
    }
}
