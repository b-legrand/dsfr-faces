package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.util.Components;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Standard {@link AjaxBehavior} (the behavior behind {@code f:ajax}, rendered by the Faces implementation)
 * with RichFaces id resolution for {@code execute} and {@code render}: an id that the standard relative
 * lookup cannot find is searched in enclosing naming containers, then in the whole view, and sent as an
 * absolute id. So {@code render="summary"} works when {@code summary} is outside the form, as in RichFaces.
 * <p>
 * No extra state: it is saved and restored as a plain {@code AjaxBehavior}.
 */
public class RichAjaxBehavior extends AjaxBehavior {

    /** Component whose script is being rendered; only set during {@link #getScript}. */
    private transient UIComponent scriptComponent;

    @Override
    public String getScript(ClientBehaviorContext behaviorContext) {
        UIComponent previous = scriptComponent;
        scriptComponent = behaviorContext.getComponent();
        try {
            return super.getScript(behaviorContext);
        } finally {
            scriptComponent = previous;
        }
    }

    @Override
    public Collection<String> getExecute() {
        return resolve(super.getExecute());
    }

    @Override
    public Collection<String> getRender() {
        return resolve(super.getRender());
    }

    private Collection<String> resolve(Collection<String> ids) {
        if (scriptComponent == null || ids == null || ids.isEmpty()) {
            return ids;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        char separator = UINamingContainer.getSeparatorChar(context);
        List<String> resolved = new ArrayList<>(ids.size());
        for (String id : ids) {
            if (id.isEmpty() || id.charAt(0) == '@' || id.charAt(0) == separator || findsRelative(id)) {
                resolved.add(id);
                continue;
            }
            UIComponent target = Components.find(context, scriptComponent, id);
            // unknown ids are kept: the Faces implementation reports them
            resolved.add(target == null ? id : separator + target.getClientId(context));
        }
        return resolved;
    }

    private boolean findsRelative(String id) {
        try {
            return scriptComponent.findComponent(id) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
