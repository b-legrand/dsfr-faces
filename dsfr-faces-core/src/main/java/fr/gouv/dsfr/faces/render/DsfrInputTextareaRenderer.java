package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrInputTextarea;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/** {@code <textarea class="fr-input">} in a {@code fr-input-group} (see {@link DsfrFieldRendererBase}). */
@FacesRenderer(componentFamily = UIInput.COMPONENT_FAMILY, rendererType = DsfrInputTextarea.RENDERER_TYPE)
public class DsfrInputTextareaRenderer extends DsfrFieldRendererBase {

    @Override
    protected String groupKind() {
        return "input";
    }

    @Override
    protected void encodeControl(FacesContext context, UIInput input, String controlId, String messagesId,
                                 String state) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement("textarea", input);
        writeControlAttributes(context, input, controlId, messagesId, state, "fr-input");
        attr(w, "rows", str(input, "rows"));
        attr(w, "placeholder", str(input, "placeholder"));
        attr(w, "maxlength", str(input, "maxlength"));
        w.writeText(currentValue(context, input), "value");
        w.endElement("textarea");
    }
}
