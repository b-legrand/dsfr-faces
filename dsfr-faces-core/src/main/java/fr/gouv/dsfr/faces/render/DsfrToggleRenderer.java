package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrToggle;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UISelectBoolean;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;

/**
 * DSFR toggle switch:
 * <pre>{@code
 * <div class="fr-toggle" id="form-notif">
 *   <input type="checkbox" class="fr-toggle__input" id="form-notif-input" name="form-notif" value="true" …>
 *   <label class="fr-toggle__label" for="form-notif-input" data-fr-checked-label="Activé"
 *          data-fr-unchecked-label="Désactivé">Notifications</label>
 *   <p class="fr-hint-text" id="form-notif-hint">…</p>
 *   <div class="fr-messages-group" id="form-notif-messages" aria-live="polite"></div>
 * </div>
 * }</pre>
 */
@FacesRenderer(componentFamily = UISelectBoolean.COMPONENT_FAMILY, rendererType = DsfrToggle.RENDERER_TYPE)
public class DsfrToggleRenderer extends DsfrSelectBooleanCheckboxRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        UIInput input = (UIInput) component;
        String id = input.getClientId(context);
        List<FacesMessage> messages = context.getMessageList(id);
        String state = groupState(input, messages);
        ResponseWriter w = context.getResponseWriter();

        w.startElement("div", null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-toggle",
                bool(input, "labelLeft") ? "fr-toggle--label-left" : null, str(input, "styleClass")), null);
        writeCheckbox(context, input, id, state, "fr-toggle__input");

        w.startElement("label", null);
        w.writeAttribute("class", "fr-toggle__label", null);
        w.writeAttribute("for", id + "-input", null);
        String checked = str(input, "checkedLabel");
        String unchecked = str(input, "uncheckedLabel");
        w.writeAttribute("data-fr-checked-label", checked == null ? "Activé" : checked, null);
        w.writeAttribute("data-fr-unchecked-label", unchecked == null ? "Désactivé" : unchecked, null);
        String label = str(input, "label");
        if (label != null) {
            w.writeText(label + (input.isRequired() ? " *" : ""), null);
        }
        w.endElement("label");

        String hint = str(input, "hint");
        if (!isBlank(hint)) {
            w.startElement("p", null);
            w.writeAttribute("class", "fr-hint-text", null);
            w.writeAttribute("id", id + "-hint", null);
            w.writeText(hint, null);
            w.endElement("p");
        }
        writeMessagesGroup(w, null, id + "-messages", null, messages);
        w.endElement("div");
    }
}
