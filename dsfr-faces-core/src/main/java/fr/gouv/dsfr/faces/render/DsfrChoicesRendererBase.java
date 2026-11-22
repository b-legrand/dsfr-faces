package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.util.SelectItems;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Radio buttons and checkboxes in a DSFR fieldset:
 * <pre>{@code
 * <fieldset class="fr-fieldset fr-fieldset--error" id="form-x" aria-labelledby="form-x-legend form-x-messages" role="group">
 *   <legend class="fr-fieldset__legend--regular fr-fieldset__legend" id="form-x-legend">Légende *<span class="fr-hint-text">…</span></legend>
 *   <div class="fr-fieldset__element"><div class="fr-radio-group">
 *     <input type="radio" id="form-x-0" name="form-x" value="a"><label class="fr-label" for="form-x-0">A</label>
 *   </div></div>
 *   <div class="fr-messages-group" id="form-x-messages" aria-live="polite">…</div>
 * </fieldset>
 * }</pre>
 * The fieldset carries the client id (ajax source and render target). {@code inline="true"}: items on one
 * line. Item descriptions are rendered as hints.
 */
public abstract class DsfrChoicesRendererBase extends DsfrInputRendererBase {

    protected static final Map<String, String> DOM_EVENTS = Map.of("valueChange", "change");

    /** radio or checkbox. */
    protected abstract String inputType();

    /** String values currently selected (submitted values after a failed validation). */
    protected abstract Set<String> selectedValues(FacesContext context, UIInput input);

    /** Rich radio: pictogram from the item description. */
    protected boolean isRich(UIInput input) {
        return false;
    }

    @Override
    protected boolean isNotSubmittable(UIInput input) {
        return bool(input, "disabled") || bool(input, "readonly");
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        UIInput input = (UIInput) component;
        String id = input.getClientId(context);
        List<FacesMessage> messages = context.getMessageList(id);
        String state = groupState(input, messages);
        ResponseWriter w = context.getResponseWriter();
        boolean rich = isRich(input);

        w.startElement("fieldset", input);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-fieldset", state == null ? null : "fr-fieldset--" + state,
                str(input, "styleClass")), null);
        w.writeAttribute("aria-labelledby", id + "-legend " + id + "-messages", null);
        if ("error".equals(state)) {
            w.writeAttribute("role", "group", null);
        }

        w.startElement("legend", null);
        w.writeAttribute("class", "fr-fieldset__legend--regular fr-fieldset__legend", null);
        w.writeAttribute("id", id + "-legend", null);
        String label = str(input, "label");
        if (label != null) {
            w.writeText(label + (input.isRequired() ? " *" : ""), null);
        }
        String hint = str(input, "hint");
        if (!isBlank(hint)) {
            w.startElement("span", null);
            w.writeAttribute("class", "fr-hint-text", null);
            w.writeText(hint, null);
            w.endElement("span");
        }
        w.endElement("legend");

        Set<String> selected = selectedValues(context, input);
        String group = "fr-" + inputType() + "-group";
        int i = 0;
        for (SelectItem item : flatten(SelectItems.collect(context, input))) {
            String value = DsfrFieldRendererBase.itemString(context, input, item.getValue());
            String itemId = id + "-" + i++;
            w.startElement("div", null);
            w.writeAttribute("class", classes("fr-fieldset__element",
                    bool(input, "inline") ? "fr-fieldset__element--inline" : null), null);
            w.startElement("div", null);
            w.writeAttribute("class", classes(group, rich ? "fr-radio-rich" : null), null);

            w.startElement("input", null);
            w.writeAttribute("type", inputType(), null);
            w.writeAttribute("id", itemId, null);
            w.writeAttribute("name", id, null);
            w.writeAttribute("value", value, null);
            if (selected.contains(value)) {
                w.writeAttribute("checked", "checked", null);
            }
            if (item.isDisabled() || bool(input, "disabled")) {
                w.writeAttribute("disabled", "disabled", null);
            }
            renderBehaviors(context, input, id, DOM_EVENTS, false);
            w.endElement("input");

            w.startElement("label", null);
            w.writeAttribute("class", "fr-label", null);
            w.writeAttribute("for", itemId, null);
            w.writeText(item.getLabel(), null);
            if (!rich && !isBlank(item.getDescription())) {
                w.startElement("span", null);
                w.writeAttribute("class", "fr-hint-text", null);
                w.writeText(item.getDescription(), null);
                w.endElement("span");
            }
            w.endElement("label");
            if (rich && !isBlank(item.getDescription())) {
                writePictogram(w, item.getDescription());
            }
            w.endElement("div");
            w.endElement("div");
        }

        writeMessagesGroup(w, null, id + "-messages", null, messages);
        w.endElement("fieldset");
    }

    /** {@code <div class="fr-radio-rich__pictogram"><svg class="fr-artwork">…</svg></div>} (DSFR pictogram). */
    private static void writePictogram(ResponseWriter w, String url) throws IOException {
        w.startElement("div", null);
        w.writeAttribute("class", "fr-radio-rich__pictogram", null);
        w.startElement("svg", null);
        w.writeAttribute("aria-hidden", "true", null);
        w.writeAttribute("class", "fr-artwork", null);
        w.writeAttribute("viewBox", "0 0 80 80", null);
        w.writeAttribute("width", "80px", null);
        w.writeAttribute("height", "80px", null);
        for (String part : List.of("decorative", "minor", "major")) {
            w.startElement("use", null);
            w.writeAttribute("class", "fr-artwork-" + part, null);
            w.writeURIAttribute("href", url + "#artwork-" + part, null);
            w.endElement("use");
        }
        w.endElement("svg");
        w.endElement("div");
    }

    private static List<SelectItem> flatten(List<SelectItem> items) {
        List<SelectItem> flat = new ArrayList<>();
        for (SelectItem item : items) {
            if (item instanceof SelectItemGroup group) {
                flat.addAll(flatten(List.of(group.getSelectItems())));
            } else {
                flat.add(item);
            }
        }
        return flat;
    }
}
