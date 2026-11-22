package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.application.ResourceDependency;

/**
 * {@code <dsfr:dataScroller for="table">}: DSFR pagination ({@code fr-pagination}) of a {@code dsfr:dataTable} (or any {@code UIData}) with {@code rows}. Pages are changed with ajax. Replaces {@code rich:dataScroller}.
 * <p>
 * Attributes read by the renderer: for, maxPages (default 10), boundaryControls (show/hide), stepControls (show/hide), page (value expression of the current page).
 */
@ResourceDependency(library = "jakarta.faces", name = "faces.js")
@FacesComponent(DsfrDataScroller.COMPONENT_TYPE)
public class DsfrDataScroller extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.DataScroller";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.DataScrollerRenderer";

    public DsfrDataScroller() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }
}
