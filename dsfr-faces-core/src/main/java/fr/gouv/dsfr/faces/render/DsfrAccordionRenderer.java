package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrAccordion;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.Objects;

/**
 * {@code <div id="form-acc" class="fr-accordions-group"><input type="hidden" name="form-acc-active" …>…items…</div>}.
 * The hidden field holds the open item, updated by the item buttons and read back on decode.
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrAccordion.RENDERER_TYPE)
public class DsfrAccordionRenderer extends DsfrRendererBase {

    static String hiddenId(FacesContext context, UIComponent accordion) {
        return accordion.getClientId(context) + "-active";
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        String active = context.getExternalContext().getRequestParameterMap().get(hiddenId(context, component));
        if (active != null) {
            DsfrAccordion accordion = (DsfrAccordion) component;
            String value = active.isEmpty() ? null : active;
            if (!Objects.equals(value, accordion.getActiveItem())) {
                accordion.setActiveItem(value);
            }
        }
    }

    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        DsfrAccordion accordion = (DsfrAccordion) component;
        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", accordion);
        w.writeAttribute("id", accordion.getClientId(context), null);
        w.writeAttribute("class", classes("fr-accordions-group", str(accordion, "styleClass")), null);
        String hidden = hiddenId(context, accordion);
        w.startElement("input", null);
        w.writeAttribute("type", "hidden", null);
        w.writeAttribute("id", hidden, null);
        w.writeAttribute("name", hidden, null);
        String active = accordion.getActiveItem();
        w.writeAttribute("value", active == null ? "" : active, null);
        w.endElement("input");
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter().endElement("div");
    }
}
