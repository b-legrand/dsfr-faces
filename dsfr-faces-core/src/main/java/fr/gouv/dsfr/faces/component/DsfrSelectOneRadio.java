package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:selectOneRadio>}: DSFR radio buttons in a fieldset. Replaces {@code h:selectOneRadio}. {@code rich="true"}: rich radios, the item description being the URL of a DSFR pictogram (svg).
 * <p>
 * Simple rendering attributes (label (legend), hint, inline, rich, disabled, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrSelectOneRadio.COMPONENT_TYPE)
public class DsfrSelectOneRadio extends UISelectOne implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.SelectOneRadio";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.SelectOneRadioRenderer";

    public DsfrSelectOneRadio() {
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
