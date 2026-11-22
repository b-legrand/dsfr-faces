package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:fieldset>}: DSFR fieldset ({@code fr-fieldset}) grouping fields under a legend, each child in a {@code fr-fieldset__element}. Error state when a field inside is invalid; messages attached to the fieldset id are shown under it.
 * <p>
 * Attributes read by the renderer: legend (or facet legend), hint, inline, styleClass.
 */
@FacesComponent(DsfrFieldset.COMPONENT_TYPE)
public class DsfrFieldset extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Fieldset";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.FieldsetRenderer";

    public DsfrFieldset() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }
}
