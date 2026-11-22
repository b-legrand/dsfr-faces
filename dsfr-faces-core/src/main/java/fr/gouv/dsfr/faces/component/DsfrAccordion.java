package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:accordion>}: group of {@code dsfr:accordionItem} ({@code fr-accordions-group}), one open at a
 * time. Replaces {@code rich:accordion} (client switching). The open item ({@code activeItem}, an item name)
 * is kept across requests through a hidden field, and written back to its value expression if any.
 * <p>
 * Attributes read by the renderer: styleClass.
 */
@FacesComponent(DsfrAccordion.COMPONENT_TYPE)
public class DsfrAccordion extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Accordion";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.AccordionRenderer";

    public DsfrAccordion() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }

    /** Name of the open item, or null. */
    public String getActiveItem() {
        return (String) getStateHelper().eval("activeItem");
    }

    public void setActiveItem(String activeItem) {
        Expressions.set(this, getStateHelper(), "activeItem", activeItem);
    }
}
