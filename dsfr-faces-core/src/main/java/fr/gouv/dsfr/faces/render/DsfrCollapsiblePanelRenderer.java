package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrCollapsiblePanel;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * A single {@code fr-accordion} (same markup as an accordion item), with a hidden field holding the state.
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrCollapsiblePanel.RENDERER_TYPE)
public class DsfrCollapsiblePanelRenderer extends DsfrRendererBase {

    private static String hiddenId(FacesContext context, UIComponent component) {
        return component.getClientId(context) + "-expanded";
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        String value = context.getExternalContext().getRequestParameterMap().get(hiddenId(context, component));
        DsfrCollapsiblePanel panel = (DsfrCollapsiblePanel) component;
        if (value != null && Boolean.parseBoolean(value) != panel.isExpanded()) {
            panel.setExpanded(Boolean.parseBoolean(value));
        }
    }

    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        DsfrCollapsiblePanel panel = (DsfrCollapsiblePanel) component;
        String hidden = hiddenId(context, panel);
        String onclick = "document.getElementById(" + jsString(hidden)
                + ").value=this.getAttribute('aria-expanded')==='true'?'false':'true'";
        DsfrAccordionItemRenderer.writeStart(context, panel, panel.getClientId(context), panel.getId(),
                panel.isExpanded(), onclick, hidden);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.endElement("div");
        w.endElement("section");
    }
}
