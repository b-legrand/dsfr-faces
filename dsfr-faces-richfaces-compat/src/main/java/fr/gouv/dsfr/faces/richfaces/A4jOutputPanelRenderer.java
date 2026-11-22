package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.render.DsfrRendererBase;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/** {@code <span id="form-p" class="…" style="…">…</span>}, or {@code div} with {@code layout="block"}. */
@FacesRenderer(componentFamily = UIPanel.COMPONENT_FAMILY, rendererType = A4jOutputPanel.RENDERER_TYPE)
public class A4jOutputPanelRenderer extends DsfrRendererBase {

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement(element(component), component);
        w.writeAttribute("id", component.getClientId(context), null);
        attr(w, "class", str(component, "styleClass"));
        attr(w, "style", str(component, "style"));
        attr(w, "title", str(component, "title"));
        attr(w, "dir", str(component, "dir"));
        attr(w, "lang", str(component, "lang"));
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        for (UIComponent child : component.getChildren()) {
            child.encodeAll(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter().endElement(element(component));
    }

    private static String element(UIComponent component) {
        return "block".equals(str(component, "layout")) ? "div" : "span";
    }
}
