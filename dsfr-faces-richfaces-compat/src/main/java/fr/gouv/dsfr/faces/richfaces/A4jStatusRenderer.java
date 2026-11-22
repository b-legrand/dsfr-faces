package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.render.DsfrRendererBase;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * <pre>{@code
 * <span id="form-status" class="dsfr-faces-status" role="status" data-a4j-status="">
 *   <span data-a4j-status-state="start" hidden>Chargement…</span>
 *   <span data-a4j-status-state="stop"></span>
 *   <span data-a4j-status-state="error" hidden>Erreur</span>
 * </span>
 * }</pre>
 * {@code div} elements with {@code layout="block"}. The error part is rendered only if there is an
 * {@code errorText} or an {@code error} facet; otherwise an error shows the "stop" part.
 * {@code onstart}, {@code onstop}, {@code onsuccess} and {@code onerror} are kept in data attributes and run
 * by richfaces.js.
 */
@FacesRenderer(componentFamily = A4jStatus.COMPONENT_FAMILY, rendererType = A4jStatus.RENDERER_TYPE)
public class A4jStatusRenderer extends DsfrRendererBase {

    private static final String[] CALLBACKS = {"onstart", "onstop", "onsuccess", "onerror"};

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        String element = "block".equals(str(component, "layout")) ? "div" : "span";
        String name = str(component, "name");
        ResponseWriter w = context.getResponseWriter();
        w.startElement(element, component);
        w.writeAttribute("id", component.getClientId(context), null);
        w.writeAttribute("class", classes("dsfr-faces-status", str(component, "styleClass")), null);
        attr(w, "style", str(component, "style"));
        w.writeAttribute("role", "status", null);
        w.writeAttribute("data-a4j-status", name == null ? "" : name, null);
        for (String callback : CALLBACKS) {
            attr(w, "data-" + callback, str(component, callback));
        }
        state(context, w, component, element, "start", true);
        state(context, w, component, element, "stop", false);
        UIComponent errorFacet = component.getFacet("error");
        if ((errorFacet != null && errorFacet.isRendered()) || !isBlank(str(component, "errorText"))) {
            state(context, w, component, element, "error", true);
        }
        w.endElement(element);
    }

    private static void state(FacesContext context, ResponseWriter w, UIComponent component, String element,
                              String state, boolean hidden) throws IOException {
        w.startElement(element, null);
        w.writeAttribute("data-a4j-status-state", state, null);
        attr(w, "class", str(component, state + "StyleClass"));
        attr(w, "style", str(component, state + "Style"));
        if (hidden) {
            w.writeAttribute("hidden", "hidden", null);
        }
        facetOrText(context, component, state, str(component, state + "Text"));
        w.endElement(element);
    }
}
