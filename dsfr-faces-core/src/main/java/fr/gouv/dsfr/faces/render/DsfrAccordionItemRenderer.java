package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrAccordion;
import fr.gouv.dsfr.faces.component.DsfrAccordionItem;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.Objects;

/**
 * <pre>{@code
 * <section class="fr-accordion" id="form-item1">
 *   <h3 class="fr-accordion__title"><button type="button" class="fr-accordion__btn" aria-expanded="false"
 *       aria-controls="form-item1-collapse">Titre</button></h3>
 *   <div class="fr-collapse" id="form-item1-collapse">…children…</div>
 * </section>
 * }</pre>
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrAccordionItem.RENDERER_TYPE)
public class DsfrAccordionItemRenderer extends DsfrRendererBase {

    static String nameOf(UIComponent item) {
        String name = str(item, "name");
        return isBlank(name) ? item.getId() : name;
    }

    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        String id = component.getClientId(context);
        String name = nameOf(component);
        DsfrAccordion accordion = component.getParent() instanceof DsfrAccordion a ? a : null;
        boolean expanded = accordion != null && Objects.equals(name, accordion.getActiveItem());
        String onclick = accordion == null ? null
                : "document.getElementById(" + jsString(DsfrAccordionRenderer.hiddenId(context, accordion))
                + ").value=this.getAttribute('aria-expanded')==='true'?'':" + jsString(name);
        writeStart(context, component, id, name, expanded, onclick, null);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.endElement("div");
        w.endElement("section");
    }

    /** Section, heading with its button, and the start of the collapse (shared with collapsiblePanel). */
    static void writeStart(FacesContext context, UIComponent component, String id, String name, boolean expanded,
                           String onclick, String hiddenField) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement("section", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-accordion", str(component, "styleClass")), null);
        if (hiddenField != null) {
            w.startElement("input", null);
            w.writeAttribute("type", "hidden", null);
            w.writeAttribute("id", hiddenField, null);
            w.writeAttribute("name", hiddenField, null);
            w.writeAttribute("value", String.valueOf(expanded), null);
            w.endElement("input");
        }
        String tag = headingTag(component, "headingTag", "h3");
        w.startElement(tag, null);
        w.writeAttribute("class", "fr-accordion__title", null);
        w.startElement("button", null);
        w.writeAttribute("type", "button", null);
        w.writeAttribute("class", "fr-accordion__btn", null);
        w.writeAttribute("aria-expanded", String.valueOf(expanded), null);
        w.writeAttribute("aria-controls", id + "-collapse", null);
        w.writeAttribute("data-dsfr-faces-item", name, null);
        if (bool(component, "disabled")) {
            w.writeAttribute("disabled", "disabled", null);
        }
        attr(w, "onclick", onclick);
        facetOrText(context, component, "header", str(component, "header"));
        w.endElement("button");
        w.endElement(tag);
        w.startElement("div", null);
        w.writeAttribute("class", "fr-collapse", null);
        w.writeAttribute("id", id + "-collapse", null);
    }
}
