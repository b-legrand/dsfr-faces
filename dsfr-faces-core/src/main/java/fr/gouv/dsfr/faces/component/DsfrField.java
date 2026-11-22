package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIMessage;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.html.HtmlOutputLabel;

import java.util.Objects;

/**
 * {@code <dsfr:field>}: DSFR field group ({@code fr-input-group} / {@code fr-select-group}) around existing
 * standard components, without changing their ids:
 * <pre>{@code
 * <dsfr:field id="loginField" hint="3 à 20 caractères">
 *   <h:outputLabel for="login" value="Identifiant"/>
 *   <h:inputText id="login" value="#{bean.login}" required="true"/>
 *   <h:message for="login"/>          (or rich:message, dsfr:message)
 * </dsfr:field>
 * }</pre>
 * The group gets the error state of its field. With the dsfr-faces-html module, the label also gets the
 * required mark and the hint, and the field {@code aria-required} / {@code aria-describedby} (message).
 * <p>
 * Ajax: the group is re-rendered automatically when its field is executed (its submitted value is then
 * redisplayed, nothing typed is lost), except for typing events (keyup, keydown, keypress, input) sent
 * by the field itself, so as not to replace an input being typed in.
 */
@FacesComponent(DsfrField.COMPONENT_TYPE)
public class DsfrField extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Field";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.FieldRenderer";

    enum PropertyKeys { hint, styleClass }

    public DsfrField() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }

    /** Additional description, rendered in the label ({@code fr-hint-text}). Needs dsfr-faces-html. */
    public String getHint() {
        return (String) getStateHelper().eval(PropertyKeys.hint);
    }

    public void setHint(String hint) {
        getStateHelper().put(PropertyKeys.hint, hint);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }

    /** The field of the group: the first {@link UIInput} child, or {@code null}. */
    public UIInput getInput() {
        for (UIComponent child : getChildren()) {
            if (child instanceof UIInput input) {
                return input;
            }
        }
        return null;
    }

    /** The message child whose {@code for} is the id of {@code input}, or {@code null}. */
    public UIMessage getMessage(UIInput input) {
        for (UIComponent child : getChildren()) {
            if (child instanceof UIMessage message && Objects.equals(message.getFor(), input.getId())) {
                return message;
            }
        }
        return null;
    }

    /** Whether {@code label} is the label of the group's field. */
    public boolean isLabelOfInput(HtmlOutputLabel label) {
        UIInput input = getInput();
        return input != null && Objects.equals(label.getFor(), input.getId());
    }

    /** select for a list field, input otherwise. */
    public String getGroupType() {
        UIInput input = getInput();
        return input instanceof UISelectOne || input instanceof UISelectMany ? "select" : "input";
    }
}
