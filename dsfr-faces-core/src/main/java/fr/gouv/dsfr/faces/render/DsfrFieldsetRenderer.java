package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrFieldset;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>{@code
 * <fieldset class="fr-fieldset fr-fieldset--error" id="form-address" aria-labelledby="form-address-legend form-address-messages">
 *   <legend class="fr-fieldset__legend" id="form-address-legend">Adresse<span class="fr-hint-text">…</span></legend>
 *   <div class="fr-fieldset__element">…child…</div>
 *   <div class="fr-messages-group" id="form-address-messages" aria-live="polite">…</div>
 * </fieldset>
 * }</pre>
 * Transient children (literal text) are not wrapped.
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrFieldset.RENDERER_TYPE)
public class DsfrFieldsetRenderer extends DsfrRendererBase {

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        String id = component.getClientId(context);
        List<FacesMessage> messages = context.getMessageList(id);
        boolean error = hasError(messages) || hasInvalidInput(component);
        boolean inline = bool(component, "inline");
        ResponseWriter w = context.getResponseWriter();

        w.startElement("fieldset", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-fieldset", error ? "fr-fieldset--error" : null,
                str(component, "styleClass")), null);
        w.writeAttribute("aria-labelledby", id + "-legend " + id + "-messages", null);
        if (error) {
            w.writeAttribute("role", "group", null);
        }

        w.startElement("legend", null);
        w.writeAttribute("class", "fr-fieldset__legend", null);
        w.writeAttribute("id", id + "-legend", null);
        facetOrText(context, component, "legend", str(component, "legend"));
        String hint = str(component, "hint");
        if (!isBlank(hint)) {
            w.startElement("span", null);
            w.writeAttribute("class", "fr-hint-text", null);
            w.writeText(hint, null);
            w.endElement("span");
        }
        w.endElement("legend");

        for (UIComponent child : component.getChildren()) {
            if (!child.isRendered()) {
                continue;
            }
            if (child.isTransient()) {
                child.encodeAll(context);
                continue;
            }
            w.startElement("div", null);
            w.writeAttribute("class", classes("fr-fieldset__element", inline ? "fr-fieldset__element--inline" : null), null);
            child.encodeAll(context);
            w.endElement("div");
        }

        writeMessagesGroup(w, null, id + "-messages", null, messages);
        w.endElement("fieldset");
    }

    private static boolean hasInvalidInput(UIComponent component) {
        Iterator<UIComponent> it = component.getFacetsAndChildren();
        while (it.hasNext()) {
            UIComponent child = it.next();
            if ((child instanceof UIInput input && !input.isValid()) || hasInvalidInput(child)) {
                return true;
            }
        }
        return false;
    }
}
