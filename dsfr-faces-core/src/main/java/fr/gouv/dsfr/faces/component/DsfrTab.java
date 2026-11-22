package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:tab>}: one tab of a {@code dsfr:tabPanel}, rendering its panel ({@code fr-tabs__panel}). Replaces {@code rich:tab}.
 * <p>
 * Attributes read by the renderer: header (or facet header), name (default: id), icon, disabled.
 */
@FacesComponent(DsfrTab.COMPONENT_TYPE)
public class DsfrTab extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Tab";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.TabRenderer";

    public DsfrTab() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }
}
