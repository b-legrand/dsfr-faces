package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:inputTextarea>}: DSFR multi-line field ({@code textarea.fr-input}). Replaces {@code h:inputTextarea}.
 * <p>
 * Simple rendering attributes (label, hint, rows, placeholder, maxlength, disabled, readonly, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrInputTextarea.COMPONENT_TYPE)
public class DsfrInputTextarea extends UIInput implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.InputTextarea";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.InputTextareaRenderer";

    public DsfrInputTextarea() {
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
