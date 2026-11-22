package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * <pre>{@code
 * <span id="form-refresh" hidden><script>window['refresh']=function(){RichFaces.ajax.send('form-refresh',
 *   {execute:'@form',render:'form-list',params:{'id':arguments[0]}})};</script></span>
 * }</pre>
 */
@FacesRenderer(componentFamily = UICommand.COMPONENT_FAMILY, rendererType = A4jJsFunction.RENDERER_TYPE)
public class A4jJsFunctionRenderer extends A4jRequestRendererBase {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        String id = component.getClientId(context);
        String name = str(component, "name");
        ResponseWriter w = context.getResponseWriter();
        w.startElement("span", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("hidden", "hidden", null);
        if (!isBlank(name)) {
            String options = options(context, component, AjaxIds.REGION_OR_FORM,
                    (index, param) -> "arguments[" + index + "]");
            w.startElement("script", null);
            w.writeText("", null); // closes the start tag
            w.write("window[" + jsString(name) + "]=function(){RichFaces.ajax.send(" + jsString(id) + ","
                    + options + ")};");
            w.endElement("script");
        }
        w.endElement("span");
    }
}
