package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrInputSecret;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;

/**
 * DSFR password field (the DSFR script switches the type when « Afficher » is checked):
 * <pre>{@code
 * <div class="fr-password" id="form-pwd">
 *   <label class="fr-password__label fr-label" for="form-pwd-input">Mot de passe *</label>
 *   <div class="fr-input-wrap"><input class="fr-password__input fr-input" type="password" …></div>
 *   <div class="fr-messages-group" id="form-pwd-messages" aria-live="polite"></div>
 *   <div class="fr-password__checkbox fr-checkbox-group fr-checkbox-group--sm">
 *     <input aria-label="Afficher le mot de passe" id="form-pwd-show" type="checkbox">
 *     <label class="fr--password__checkbox fr-label" for="form-pwd-show">Afficher</label>
 *   </div>
 * </div>
 * }</pre>
 * The value is not redisplayed unless {@code redisplay="true"}. Without label: a bare
 * {@code input.fr-input} carrying the client id.
 */
@FacesRenderer(componentFamily = UIInput.COMPONENT_FAMILY, rendererType = DsfrInputSecret.RENDERER_TYPE)
public class DsfrInputSecretRenderer extends DsfrFieldRendererBase {

    @Override
    protected String groupKind() {
        return "input";
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
        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", "fr-password", null);

        w.startElement("label", null);
        w.writeAttribute("class", "fr-password__label fr-label", null);
        w.writeAttribute("for", id + "-input", null);
        w.writeText(label + (input.isRequired() ? " *" : ""), null);
        String hint = str(input, "hint");
        if (!isBlank(hint)) {
            w.startElement("span", null);
            w.writeAttribute("class", "fr-hint-text", null);
            w.writeText(hint, null);
            w.endElement("span");
        }
        w.endElement("label");

        w.startElement("div", null);
        w.writeAttribute("class", "fr-input-wrap", null);
        encodeControl(context, input, id + "-input", id + "-messages", state);
        w.endElement("div");

        writeMessagesGroup(w, null, id + "-messages", null, messages);

        w.startElement("div", null);
        w.writeAttribute("class", "fr-password__checkbox fr-checkbox-group fr-checkbox-group--sm", null);
        w.startElement("input", null);
        w.writeAttribute("aria-label", "Afficher le mot de passe", null);
        w.writeAttribute("id", id + "-show", null);
        w.writeAttribute("type", "checkbox", null);
        w.endElement("input");
        w.startElement("label", null);
        w.writeAttribute("class", "fr--password__checkbox fr-label", null);
        w.writeAttribute("for", id + "-show", null);
        w.writeText("Afficher", null);
        w.endElement("label");
        w.endElement("div");

        w.endElement("div");
    }

    @Override
    protected void encodeControl(FacesContext context, UIInput input, String controlId, String messagesId,
                                 String state) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        boolean group = messagesId != null;
        w.startElement("input", input);
        w.writeAttribute("type", "password", null);
        writeControlAttributes(context, input, controlId, messagesId, state,
                group ? "fr-password__input fr-input" : "fr-input");
        w.writeAttribute("autocapitalize", "off", null);
        w.writeAttribute("autocorrect", "off", null);
        attr(w, "autocomplete", str(input, "autocomplete"));
        w.writeAttribute("value", bool(input, "redisplay") ? currentValue(context, input) : "", "value");
        w.endElement("input");
    }
}
