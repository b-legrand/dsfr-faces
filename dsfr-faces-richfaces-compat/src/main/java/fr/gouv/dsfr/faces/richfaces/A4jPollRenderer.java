package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * <pre>{@code
 * <span id="form-poll" hidden><script>RichFaces.ajax.poll('form-poll',5000,true,function(){…ontimer…},
 *   {execute:'@this',render:'form-x'});</script></span>
 * }</pre>
 */
@FacesRenderer(componentFamily = UICommand.COMPONENT_FAMILY, rendererType = A4jPoll.RENDERER_TYPE)
public class A4jPollRenderer extends A4jRequestRendererBase {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        String id = component.getClientId(context);
        Object enabledValue = component.getAttributes().get("enabled");
        boolean enabled = enabledValue == null || Boolean.TRUE.equals(enabledValue) || "true".equals(enabledValue);
        int interval = interval(component);
        String ontimer = str(component, "ontimer");
        ResponseWriter w = context.getResponseWriter();
        w.startElement("span", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("hidden", "hidden", null);
        w.startElement("script", null);
        w.writeText("", null);
        w.write("RichFaces.ajax.poll(" + jsString(id) + "," + interval + "," + enabled + ","
                + (isBlank(ontimer) ? "null" : "function(){" + ontimer + "\n}") + ","
                + (enabled ? options(context, component, AjaxIds.REGION_OR_THIS, (i, p) -> literalValue(p)) : "null")
                + ");");
        w.endElement("script");
        w.endElement("span");
    }

    private static int interval(UIComponent component) {
        Object value = component.getAttributes().get("interval");
        if (value instanceof Number n) {
            return Math.max(n.intValue(), 100);
        }
        try {
            return value == null ? 1000 : Math.max(Integer.parseInt(value.toString().trim()), 100);
        } catch (NumberFormatException e) {
            return 1000;
        }
    }
}
