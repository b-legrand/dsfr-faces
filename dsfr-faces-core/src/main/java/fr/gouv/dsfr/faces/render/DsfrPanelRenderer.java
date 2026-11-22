package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrPanel;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * DSFR card without link:
 * <pre>{@code
 * <div id="form-panel" class="fr-card fr-card--no-arrow">
 *   <div class="fr-card__body"><div class="fr-card__content">
 *     <h3 class="fr-card__title">Titre</h3>
 *     <div class="fr-card__desc">…children…</div>
 *   </div></div>
 * </div>
 * }</pre>
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrPanel.RENDERER_TYPE)
public class DsfrPanelRenderer extends DsfrRendererBase {

    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", component);
        w.writeAttribute("id", component.getClientId(context), null);
        w.writeAttribute("class", classes("fr-card fr-card--no-arrow", str(component, "styleClass")), null);
        w.startElement("div", null);
        w.writeAttribute("class", "fr-card__body", null);
        w.startElement("div", null);
        w.writeAttribute("class", "fr-card__content", null);

        UIComponent facet = component.getFacet("header");
        String header = str(component, "header");
        if ((facet != null && facet.isRendered()) || !isBlank(header)) {
            String tag = headingTag(component, "headerTag", "h3");
            w.startElement(tag, null);
            w.writeAttribute("class", "fr-card__title", null);
            facetOrText(context, component, "header", header);
            w.endElement(tag);
        }
        w.startElement("div", null);
        w.writeAttribute("class", classes("fr-card__desc", str(component, "bodyClass")), null);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.endElement("div");
        w.endElement("div");
        w.endElement("div");
        w.endElement("div");
    }
}
