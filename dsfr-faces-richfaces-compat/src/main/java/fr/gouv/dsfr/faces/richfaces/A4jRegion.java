package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIPanel;

/**
 * {@code <a4j:region id="r">…</a4j:region>}: the part of the tree that the a4j controls inside it process
 * ({@code execute}) by default, instead of the whole form. {@code execute="@region"} names it explicitly.
 * <p>
 * Rendered as {@code <div id="…" style="display:contents">}: no effect on the layout, but faces.js needs a
 * DOM element to send the fields of an executed id (partial submit). It is also a render target.
 */
@FacesComponent(A4jRegion.COMPONENT_TYPE)
public class A4jRegion extends UIPanel {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.richfaces.Region";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.richfaces.Region";

    public A4jRegion() {
        setRendererType(RENDERER_TYPE);
    }
}
