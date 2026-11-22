package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:collapsiblePanel>}: a single DSFR accordion ({@code fr-accordion}). Replaces
 * {@code rich:collapsiblePanel} (client switching). The state ({@code expanded}) is kept across requests
 * through a hidden field, and written back to its value expression if any.
 * <p>
 * Attributes read by the renderer: header (or facet header), headingTag (default h3), styleClass.
 */
@FacesComponent(DsfrCollapsiblePanel.COMPONENT_TYPE)
public class DsfrCollapsiblePanel extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.CollapsiblePanel";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.CollapsiblePanelRenderer";

    public DsfrCollapsiblePanel() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }

    public boolean isExpanded() {
        Object value = getStateHelper().eval("expanded", Boolean.FALSE);
        return Boolean.TRUE.equals(value) || "true".equals(value);
    }

    public void setExpanded(boolean expanded) {
        Expressions.set(this, getStateHelper(), "expanded", expanded);
    }
}
