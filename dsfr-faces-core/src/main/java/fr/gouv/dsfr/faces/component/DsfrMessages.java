package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIMessages;

import java.util.Set;

/**
 * {@code <dsfr:messages>}: renders Faces messages as DSFR alerts ({@code fr-alert}).
 * Replaces {@code rich:messages} / {@code h:messages}. Inherits {@code globalOnly}, {@code for},
 * {@code showSummary}, {@code showDetail} and {@code redisplay} from {@link UIMessages}.
 * <p>
 * The wrapper {@code div} is always rendered so it can be an ajax {@code render} target.
 */
@FacesComponent(DsfrMessages.COMPONENT_TYPE)
public class DsfrMessages extends UIMessages {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Messages";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.MessagesRenderer";

    private static final Set<String> TITLE_TAGS = Set.of("h2", "h3", "h4", "h5", "h6", "p");

    enum PropertyKeys { titleTag, styleClass }

    public DsfrMessages() {
        setRendererType(RENDERER_TYPE);
    }

    /** Element used for alert titles, to fit the page heading hierarchy (RGAA 9.1). Default h3. */
    public String getTitleTag() {
        String tag = (String) getStateHelper().eval(PropertyKeys.titleTag, "h3");
        return TITLE_TAGS.contains(tag) ? tag : "h3";
    }

    public void setTitleTag(String titleTag) {
        getStateHelper().put(PropertyKeys.titleTag, titleTag);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }
}
