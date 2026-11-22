package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrPopupPanel;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * DSFR modal:
 * <pre>{@code
 * <dialog id="form-popup" class="fr-modal" aria-labelledby="form-popup-title">
 *   <div class="fr-container fr-container--fluid fr-container-md"><div class="fr-grid-row fr-grid-row--center">
 *     <div class="fr-col-12 fr-col-md-8 fr-col-lg-6"><div class="fr-modal__body">
 *       <div class="fr-modal__header"><button aria-controls="form-popup" title="Fermer" type="button"
 *           class="fr-btn--close fr-btn">Fermer</button></div>
 *       <div class="fr-modal__content"><h1 id="form-popup-title" class="fr-modal__title">Titre</h1>…</div>
 *     </div></div>
 *   </div></div>
 * </dialog>
 * }</pre>
 * {@code size}: sm (md-6 / lg-4), md (default, md-8 / lg-6), lg (md-10 / lg-8).
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrPopupPanel.RENDERER_TYPE)
public class DsfrPopupPanelRenderer extends DsfrRendererBase {

    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        String id = component.getClientId(context);
        ResponseWriter w = context.getResponseWriter();
        w.startElement("dialog", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-modal", str(component, "styleClass")), null);
        w.writeAttribute("aria-labelledby", id + "-title", null);
        if (bool(component, "show")) {
            w.writeAttribute("data-dsfr-faces-show", "true", null);
        }
        div(w, "fr-container fr-container--fluid fr-container-md");
        div(w, "fr-grid-row fr-grid-row--center");
        div(w, switch (String.valueOf(str(component, "size"))) {
            case "sm" -> "fr-col-12 fr-col-md-6 fr-col-lg-4";
            case "lg" -> "fr-col-12 fr-col-md-10 fr-col-lg-8";
            default -> "fr-col-12 fr-col-md-8 fr-col-lg-6";
        });
        div(w, "fr-modal__body");
        div(w, "fr-modal__header");
        w.startElement("button", null);
        w.writeAttribute("aria-controls", id, null);
        w.writeAttribute("title", "Fermer", null);
        w.writeAttribute("type", "button", null);
        w.writeAttribute("class", "fr-btn--close fr-btn", null);
        w.writeText("Fermer", null);
        w.endElement("button");
        w.endElement("div");
        div(w, "fr-modal__content");
        String tag = headingTag(component, "titleTag", "h1");
        w.startElement(tag, null);
        w.writeAttribute("id", id + "-title", null);
        w.writeAttribute("class", "fr-modal__title", null);
        facetOrText(context, component, "header", str(component, "header"));
        w.endElement(tag);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        for (int i = 0; i < 5; i++) {
            w.endElement("div"); // content, body, column, row, container
        }
        w.endElement("dialog");
    }

    private static void div(ResponseWriter w, String cssClass) throws IOException {
        w.startElement("div", null);
        w.writeAttribute("class", cssClass, null);
    }
}
