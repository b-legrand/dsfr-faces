package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UISelectBoolean;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:selectBooleanCheckbox>}: DSFR checkbox ({@code fr-checkbox-group}). Replaces {@code h:selectBooleanCheckbox}.
 * <p>
 * Simple rendering attributes (label, hint, size, disabled, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrSelectBooleanCheckbox.COMPONENT_TYPE)
public class DsfrSelectBooleanCheckbox extends UISelectBoolean implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.SelectBooleanCheckbox";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.SelectBooleanCheckboxRenderer";

    public DsfrSelectBooleanCheckbox() {
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
