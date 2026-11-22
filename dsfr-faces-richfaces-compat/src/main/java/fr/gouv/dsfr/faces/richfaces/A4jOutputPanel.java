package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIPanel;

/**
 * {@code <a4j:outputPanel id="p" ajaxRendered="true" layout="block">}: a {@code span} (or a {@code div}
 * with {@code layout="block"}) always rendered with its id, so that it can be an ajax render target, and
 * re-rendered by every ajax request when {@code ajaxRendered="true"}.
 */
@FacesComponent(A4jOutputPanel.COMPONENT_TYPE)
public class A4jOutputPanel extends UIPanel {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.richfaces.OutputPanel";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.richfaces.OutputPanel";

    public A4jOutputPanel() {
        setRendererType(RENDERER_TYPE);
    }
}
