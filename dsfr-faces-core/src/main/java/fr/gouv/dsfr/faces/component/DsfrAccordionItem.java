package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:accordionItem>}: one accordion of a {@code dsfr:accordion}. Replaces {@code rich:accordionItem}.
 * <p>
 * Attributes read by the renderer: header (or facet header), name (default: id), headingTag (default h3), disabled.
 */
@FacesComponent(DsfrAccordionItem.COMPONENT_TYPE)
public class DsfrAccordionItem extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.AccordionItem";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.AccordionItemRenderer";

    public DsfrAccordionItem() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }
}
