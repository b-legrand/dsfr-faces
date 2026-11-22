package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:selectOneMenu>}: DSFR drop-down list ({@code fr-select}). Replaces {@code h:selectOneMenu} and {@code rich:select}.
 * <p>
 * Simple rendering attributes (label, hint, placeholder (or RichFaces defaultLabel), disabled, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrSelectOneMenu.COMPONENT_TYPE)
public class DsfrSelectOneMenu extends UISelectOne implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.SelectOneMenu";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.SelectOneMenuRenderer";

    public DsfrSelectOneMenu() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public Collection<String> getEventNames() {
        return FieldEvents.NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return FieldEvents.DEFAULT;
    }
}
