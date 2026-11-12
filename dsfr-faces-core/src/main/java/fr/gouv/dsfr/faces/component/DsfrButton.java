package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;
import java.util.List;

/**
 * {@code <dsfr:button>}: a DSFR button ({@code fr-btn}) bound to a Faces action.
 * Replaces {@code h:commandButton} and, combined with {@code <f:ajax>}, {@code a4j:commandButton}.
 */
@FacesComponent(DsfrButton.COMPONENT_TYPE)
public class DsfrButton extends UICommand implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Button";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.ButtonRenderer";

    private static final Collection<String> EVENT_NAMES = List.of("action", "click", "focus", "blur");

    enum PropertyKeys { type, priority, size, icon, iconPosition, title, disabled, styleClass }

    public DsfrButton() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "action";
    }

    /** submit (default), button or reset. */
    public String getType() {
        return (String) getStateHelper().eval(PropertyKeys.type, "submit");
    }

    public void setType(String type) {
        getStateHelper().put(PropertyKeys.type, type);
    }

    /** primary (default), secondary, tertiary, tertiary-no-outline. */
    public String getPriority() {
        return (String) getStateHelper().eval(PropertyKeys.priority, "primary");
    }

    public void setPriority(String priority) {
        getStateHelper().put(PropertyKeys.priority, priority);
    }

    /** sm, md (default), lg. */
    public String getSize() {
        return (String) getStateHelper().eval(PropertyKeys.size, "md");
    }

    public void setSize(String size) {
        getStateHelper().put(PropertyKeys.size, size);
    }

    /** DSFR icon class, e.g. {@code fr-icon-checkbox-circle-line}. */
    public String getIcon() {
        return (String) getStateHelper().eval(PropertyKeys.icon);
    }

    public void setIcon(String icon) {
        getStateHelper().put(PropertyKeys.icon, icon);
    }

    /** left (default), right, or only (icon-only button: the label becomes the title). */
    public String getIconPosition() {
        return (String) getStateHelper().eval(PropertyKeys.iconPosition, "left");
    }

    public void setIconPosition(String iconPosition) {
        getStateHelper().put(PropertyKeys.iconPosition, iconPosition);
    }

    public String getTitle() {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }

    public void setTitle(String title) {
        getStateHelper().put(PropertyKeys.title, title);
    }

    public boolean isDisabled() {
        return (Boolean) getStateHelper().eval(PropertyKeys.disabled, Boolean.FALSE);
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }
}
