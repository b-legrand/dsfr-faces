package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:panel>}: content box with a title, rendered as a DSFR card without link ({@code fr-card fr-card--no-arrow}). Replaces {@code rich:panel}.
 * <p>
 * Attributes read by the renderer: header (or facet header), headerTag (h2..h6, default h3), styleClass, bodyClass.
 */
@FacesComponent(DsfrPanel.COMPONENT_TYPE)
public class DsfrPanel extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Panel";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.PanelRenderer";

    public DsfrPanel() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }
}
