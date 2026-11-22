package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.render.DsfrRendererBase;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/** {@code <div id="form-region" style="display:contents">…</div>}. */
@FacesRenderer(componentFamily = UIPanel.COMPONENT_FAMILY, rendererType = A4jRegion.RENDERER_TYPE)
public class A4jRegionRenderer extends DsfrRendererBase {

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", component);
        w.writeAttribute("id", component.getClientId(context), null);
        w.writeAttribute("style", "display:contents", null);
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        for (UIComponent child : component.getChildren()) {
            child.encodeAll(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter().endElement("div");
    }
}
