package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrInputText;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Renders, for clientId {@code form-email}:
 * <pre>{@code
 * <div class="fr-input-group fr-input-group--error" id="form-email">
 *   <label class="fr-label" for="form-email-input">Adresse e-mail *<span class="fr-hint-text">…</span></label>
 *   <input class="fr-input" id="form-email-input" name="form-email" type="email" value="…"
 *          aria-describedby="form-email-messages" aria-invalid="true" aria-required="true">
 *   <div class="fr-messages-group" id="form-email-messages" aria-live="polite">
 *     <p class="fr-message fr-message--error" id="form-email-messages-0">…</p>
 *   </div>
 * </div>
 * }</pre>
 * {@code aria-required} is used rather than {@code required} so that browser-native validation does not
 * pre-empt Faces validation and its DSFR error messages.
 */
@FacesRenderer(componentFamily = UIInput.COMPONENT_FAMILY, rendererType = DsfrInputText.RENDERER_TYPE)
public class DsfrInputTextRenderer extends DsfrInputRendererBase {

    private static final Map<String, String> DOM_EVENTS = Map.of("valueChange", "change");

    @Override
    protected boolean isNotSubmittable(UIInput input) {
        DsfrInputText in = (DsfrInputText) input;
        return in.isDisabled() || in.isReadonly();
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        DsfrInputText in = (DsfrInputText) component;
        String id = in.getClientId(context);
        String inputId = id + "-input";
        String messagesId = id + "-messages";
        List<FacesMessage> messages = context.getMessageList(id);
        String state = groupState(in, messages);
        String type = in.getType();

        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-input-group",
                state == null ? null : "fr-input-group--" + state,
                in.isDisabled() ? "fr-input-group--disabled" : null), null);

        writeLabel(w, inputId, in.getLabel(), in.getHint(), in.isRequired());

        w.startElement("input", in); // receives pass-through attributes
        w.writeAttribute("class", classes("fr-input", in.getStyleClass()), null);
        w.writeAttribute("id", inputId, null);
        w.writeAttribute("name", id, null);
        w.writeAttribute("type", type, null);
        w.writeAttribute("value", "password".equals(type) ? "" : currentValue(context, in), "value");
        w.writeAttribute("aria-describedby", messagesId, null);
        if ("error".equals(state)) {
            w.writeAttribute("aria-invalid", "true", null);
        }
        if (in.isRequired()) {
            w.writeAttribute("aria-required", "true", null);
        }
        if (in.isDisabled()) {
            w.writeAttribute("disabled", "disabled", null);
        }
        if (in.isReadonly()) {
            w.writeAttribute("readonly", "readonly", null);
        }
        attr(w, "placeholder", in.getPlaceholder());
        attr(w, "autocomplete", in.getAutocomplete());
        attr(w, "inputmode", in.getInputmode());
        attr(w, "maxlength", in.getMaxlength());
        // The wrapper div carries the clientId: it is the ajax source, so "@this" resolves to this component.
        renderBehaviors(context, in, id, DOM_EVENTS, false);
        w.endElement("input");

        writeMessagesGroup(w, messagesId, messages);
        w.endElement("div");
    }
}
