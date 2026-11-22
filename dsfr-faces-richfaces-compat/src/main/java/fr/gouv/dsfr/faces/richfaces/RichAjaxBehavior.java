package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;

import java.util.Collection;

/**
 * Standard {@link AjaxBehavior} (the behavior behind {@code f:ajax}, rendered by the Faces implementation)
 * with RichFaces id resolution for {@code execute} and {@code render}: an id that the standard relative
 * lookup cannot find is searched in enclosing naming containers, then in the whole view, and sent as an
 * absolute id. So {@code render="summary"} works when {@code summary} is outside the form, as in RichFaces.
 * {@code @region} and the default execute inside an {@code a4j:region} are resolved too (see {@link AjaxIds}).
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
        return AjaxIds.forBehavior(FacesContext.getCurrentInstance(), scriptComponent, ids);
    }
}
