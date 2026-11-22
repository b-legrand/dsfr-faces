package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;

/**
 * {@code <dsfr:toggle>}: DSFR toggle switch ({@code fr-toggle}), a boolean like
 * {@link DsfrSelectBooleanCheckbox}.
 * <p>
 * Attributes read by the renderer: label, hint, checkedLabel (default « Activé »), uncheckedLabel (default
 * « Désactivé »), labelLeft, disabled, styleClass.
 */
@FacesComponent(DsfrToggle.COMPONENT_TYPE)
public class DsfrToggle extends DsfrSelectBooleanCheckbox {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Toggle";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.ToggleRenderer";

    public DsfrToggle() {
        setRendererType(RENDERER_TYPE);
    }
}
