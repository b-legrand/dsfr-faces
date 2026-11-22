package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIMessage;

/**
 * {@code <dsfr:message for="…">}: the messages of one component, as DSFR inline messages
 * ({@code fr-messages-group} / {@code fr-message}). Replaces {@code rich:message} / {@code h:message}
 * next to fields that are not DSFR components (e.g. {@code h:inputText}).
 * <p>
 * The wrapper is always rendered so it can be an ajax {@code render} target, and it is
 * {@code ajaxRendered} by default (refreshed on every ajax request, as in RichFaces).
 */
@FacesComponent(DsfrMessage.COMPONENT_TYPE)
public class DsfrMessage extends UIMessage {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Message";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.MessageRenderer";

    enum PropertyKeys { styleClass, ajaxRendered }

    public DsfrMessage() {
        setRendererType(RENDERER_TYPE);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }

    /** Re-rendered by every ajax request. Default true. */
    public boolean isAjaxRendered() {
        return (Boolean) getStateHelper().eval(PropertyKeys.ajaxRendered, Boolean.TRUE);
    }

    public void setAjaxRendered(boolean ajaxRendered) {
        getStateHelper().put(PropertyKeys.ajaxRendered, ajaxRendered);
    }
}
