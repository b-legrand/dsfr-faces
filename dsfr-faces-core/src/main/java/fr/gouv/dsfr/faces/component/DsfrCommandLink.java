package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;
import java.util.List;

/**
 * {@code <dsfr:commandLink>}: action link ({@code a.fr-link}) bound to a Faces action. Replaces
 * {@code h:commandLink} and, with an ajax behavior, {@code a4j:commandLink}. Without ajax behavior the click
 * submits the enclosing form (with the {@code f:param} children).
 * <p>
 * Attributes read by the renderer: icon, iconPosition (left, right), size (sm, lg), title, disabled, onclick,
 * styleClass. The content is the {@code value}, or the children when there is no value.
 */
@FacesComponent(DsfrCommandLink.COMPONENT_TYPE)
public class DsfrCommandLink extends UICommand implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.CommandLink";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.CommandLinkRenderer";

    private static final Collection<String> EVENT_NAMES = List.of("action", "click", "focus", "blur");

    public DsfrCommandLink() {
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
}
