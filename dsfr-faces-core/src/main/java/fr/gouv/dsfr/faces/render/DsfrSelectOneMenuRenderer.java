package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrSelectOneMenu;
import fr.gouv.dsfr.faces.util.SelectItems;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;

/**
 * {@code <select class="fr-select">} in a {@code fr-select-group} (see {@link DsfrFieldRendererBase}).
 * {@code placeholder} (or {@code defaultLabel}, from {@code rich:select}) renders a first empty option,
 * disabled when the field is required, as in the DSFR examples.
 */
@FacesRenderer(componentFamily = UISelectOne.COMPONENT_FAMILY, rendererType = DsfrSelectOneMenu.RENDERER_TYPE)
public class DsfrSelectOneMenuRenderer extends DsfrFieldRendererBase {

    @Override
    protected String groupKind() {
        return "select";
    }

    @Override
    protected void encodeControl(FacesContext context, UIInput input, String controlId, String messagesId,
                                 String state) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        String current = currentValue(context, input);
        w.startElement("select", input);
        writeControlAttributes(context, input, controlId, messagesId, state, "fr-select");

        String placeholder = str(input, "placeholder");
        if (isBlank(placeholder)) {
            placeholder = str(input, "defaultLabel");
        }
        if (!isBlank(placeholder)) {
            w.startElement("option", null);
            w.writeAttribute("value", "", null);
            if (current.isEmpty()) {
                w.writeAttribute("selected", "selected", null);
            }
            if (input.isRequired()) {
                w.writeAttribute("disabled", "disabled", null);
            }
            w.writeText(placeholder, null);
            w.endElement("option");
        }
        writeOptions(context, input, SelectItems.collect(context, input), current);
        w.endElement("select");
    }

    private static void writeOptions(FacesContext context, UIInput input, List<SelectItem> items, String current)
            throws IOException {
        ResponseWriter w = context.getResponseWriter();
        for (SelectItem item : items) {
            if (item instanceof SelectItemGroup group) {
                w.startElement("optgroup", null);
                w.writeAttribute("label", group.getLabel(), null);
                writeOptions(context, input, List.of(group.getSelectItems()), current);
                w.endElement("optgroup");
                continue;
            }
            String value = itemString(context, input, item.getValue());
            w.startElement("option", null);
            w.writeAttribute("value", value, null);
            if (value.equals(current)) {
                w.writeAttribute("selected", "selected", null);
            }
            if (item.isDisabled()) {
                w.writeAttribute("disabled", "disabled", null);
            }
            w.writeText(item.getLabel(), null);
            w.endElement("option");
        }
    }
}
