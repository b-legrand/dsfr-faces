package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <dsfr:popupPanel>}: DSFR modal ({@code dialog.fr-modal}). Replaces {@code rich:popupPanel}. Opened by {@code DsfrFaces.component(id).show()} / {@code #{rich:component('id')}.show()}, or with {@code show="true"}.
 * <p>
 * Attributes read by the renderer: header (or facet header), titleTag (default h1), show, size (sm, md, lg), styleClass.
 */
@FacesComponent(DsfrPopupPanel.COMPONENT_TYPE)
public class DsfrPopupPanel extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.PopupPanel";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.PopupPanelRenderer";

    public DsfrPopupPanel() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }
}
