package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:inputSecret>}: DSFR password field ({@code fr-password}, with the « Afficher » checkbox). Replaces {@code h:inputSecret}.
 * <p>
 * Simple rendering attributes (label, hint, autocomplete, disabled, readonly, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrInputSecret.COMPONENT_TYPE)
public class DsfrInputSecret extends UIInput implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.InputSecret";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.InputSecretRenderer";

    public DsfrInputSecret() {
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
