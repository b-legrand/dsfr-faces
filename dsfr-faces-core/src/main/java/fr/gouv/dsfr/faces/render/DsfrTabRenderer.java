package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrTab;
import fr.gouv.dsfr.faces.component.DsfrTabPanel;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * Tab panel ({@code fr-tabs__panel}), carrying the tab client id so that a tab can be an ajax render target.
 * Its content is only rendered when the tab is active, or with {@code switchType="client"}.
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrTab.RENDERER_TYPE)
public class DsfrTabRenderer extends DsfrRendererBase {

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        DsfrTabPanel panel = component.getParent() instanceof DsfrTabPanel p ? p : null;
        boolean selected = panel == null || panel.getActiveTab() == component;
        String id = component.getClientId(context);
        ResponseWriter w = context.getResponseWriter();

        w.startElement("div", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-tabs__panel", selected ? "fr-tabs__panel--selected" : null), null);
        w.writeAttribute("role", "tabpanel", null);
        w.writeAttribute("aria-labelledby", DsfrTabPanelRenderer.buttonId(context, component), null);
        w.writeAttribute("tabindex", "0", null);
        if (selected || panel.isClientSwitch()) {
            for (UIComponent child : component.getChildren()) {
                if (child.isRendered()) {
                    child.encodeAll(context);
                }
            }
        }
        w.endElement("div");
    }
}
