package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:selectManyCheckbox>}: DSFR checkboxes in a fieldset. Replaces {@code h:selectManyCheckbox}.
 * <p>
 * Simple rendering attributes (label (legend), hint, inline, disabled, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrSelectManyCheckbox.COMPONENT_TYPE)
public class DsfrSelectManyCheckbox extends UISelectMany implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.SelectManyCheckbox";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.SelectManyCheckboxRenderer";

    public DsfrSelectManyCheckbox() {
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
