package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrField;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * {@code <div id="form-loginField" class="fr-input-group fr-input-group--error">…children…</div>}
 * ({@code fr-select-group} for a list).
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrField.RENDERER_TYPE)
public class DsfrFieldRenderer extends DsfrRendererBase {

    @Override
    public boolean getRendersChildren() {
        return false; // children are encoded by the component, between encodeBegin and encodeEnd
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        DsfrField field = (DsfrField) component;
        String group = "fr-" + field.getGroupType() + "-group";
        UIInput input = field.getInput();
        boolean error = input != null
                && (!input.isValid() || hasError(context.getMessageList(input.getClientId(context))));
        boolean disabled = input != null && Boolean.TRUE.equals(input.getAttributes().get("disabled"));

        ResponseWriter w = context.getResponseWriter();
        w.startElement("div", field);
        w.writeAttribute("id", field.getClientId(context), null);
        w.writeAttribute("class", classes(group,
                error ? group + "--error" : null,
                disabled ? group + "--disabled" : null,
                field.getStyleClass()), null);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter().endElement("div");
    }
}
