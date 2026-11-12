package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;
import java.util.List;

/**
 * {@code <dsfr:inputText>}: a DSFR "champ de saisie" ({@code fr-input-group}) with label, hint and
 * inline messages, wired for accessibility.
 * <p>
 * The wrapper {@code div} carries the component clientId, so {@code <f:ajax render="@this"/>} or
 * {@code render="email"} re-renders the whole group, error state included.
 */
@FacesComponent(DsfrInputText.COMPONENT_TYPE)
public class DsfrInputText extends UIInput implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.InputText";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.InputTextRenderer";

    private static final Collection<String> EVENT_NAMES =
            List.of("valueChange", "change", "blur", "focus", "input", "keydown", "keyup", "click");

    enum PropertyKeys { label, hint, type, placeholder, autocomplete, inputmode, maxlength, disabled, readonly, styleClass }

    public DsfrInputText() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "valueChange";
    }

    /** Label text. Also used by Faces validation messages ({@code {0}} placeholder). */
    public String getLabel() {
        return (String) getStateHelper().eval(PropertyKeys.label);
    }

    public void setLabel(String label) {
        getStateHelper().put(PropertyKeys.label, label);
    }

    /** Additional description rendered as {@code fr-hint-text} inside the label. */
    public String getHint() {
        return (String) getStateHelper().eval(PropertyKeys.hint);
    }

    public void setHint(String hint) {
        getStateHelper().put(PropertyKeys.hint, hint);
    }

    /** HTML input type: text (default), email, tel, url, number, date, search, password... */
    public String getType() {
        return (String) getStateHelper().eval(PropertyKeys.type, "text");
    }

    public void setType(String type) {
        getStateHelper().put(PropertyKeys.type, type);
    }

    public String getPlaceholder() {
        return (String) getStateHelper().eval(PropertyKeys.placeholder);
    }

    public void setPlaceholder(String placeholder) {
        getStateHelper().put(PropertyKeys.placeholder, placeholder);
    }

    /** HTML autocomplete token (RGAA 11.13 requires it on personal data fields). */
    public String getAutocomplete() {
        return (String) getStateHelper().eval(PropertyKeys.autocomplete);
    }

    public void setAutocomplete(String autocomplete) {
        getStateHelper().put(PropertyKeys.autocomplete, autocomplete);
    }

    public String getInputmode() {
        return (String) getStateHelper().eval(PropertyKeys.inputmode);
    }

    public void setInputmode(String inputmode) {
        getStateHelper().put(PropertyKeys.inputmode, inputmode);
    }

    public Integer getMaxlength() {
        return (Integer) getStateHelper().eval(PropertyKeys.maxlength);
    }

    public void setMaxlength(Integer maxlength) {
        getStateHelper().put(PropertyKeys.maxlength, maxlength);
    }

    public boolean isDisabled() {
        return (Boolean) getStateHelper().eval(PropertyKeys.disabled, Boolean.FALSE);
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    public boolean isReadonly() {
        return (Boolean) getStateHelper().eval(PropertyKeys.readonly, Boolean.FALSE);
    }

    public void setReadonly(boolean readonly) {
        getStateHelper().put(PropertyKeys.readonly, readonly);
    }

    /** Extra CSS classes added to the {@code <input>} element. */
    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }
}
