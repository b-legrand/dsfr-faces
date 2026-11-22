package fr.gouv.dsfr.faces.util;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

import java.util.EnumSet;

/**
 * Component lookup utilities.
 */
public final class Components {

    private Components() {
    }

    /**
     * Finds a component by id the way RichFaces did, which is more lenient than
     * {@link UIComponent#findComponent}:
     * <ol>
     *     <li>{@code from.findComponent(id)} (relative to the closest naming container);</li>
     *     <li>then from each enclosing naming container, innermost first;</li>
     *     <li>then the first component of the whole view whose id is {@code id}.</li>
     * </ol>
     *
     * @param from starting component, may be {@code null} (search from the view root)
     * @return the component, or {@code null}
     */
    public static UIComponent find(FacesContext context, UIComponent from, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        UIViewRoot root = context.getViewRoot();
        if (root == null) {
            return null;
        }
        UIComponent start = from == null ? root : from;
        UIComponent found = findQuietly(start, id);
        for (UIComponent p = start.getParent(); found == null && p != null; p = p.getParent()) {
            if (p instanceof NamingContainer) {
                found = findQuietly(p, id);
            }
        }
        if (found == null) {
            found = findFirstById(context, root, id);
        }
        return found;
    }

    private static UIComponent findQuietly(UIComponent from, String expression) {
        try {
            return from.findComponent(expression);
        } catch (IllegalArgumentException e) {
            // an intermediate id of the expression is not a naming container
            return null;
        }
    }

    private static UIComponent findFirstById(FacesContext context, UIViewRoot root, String id) {
        UIComponent[] result = new UIComponent[1];
        VisitContext visit = VisitContext.createVisitContext(context, null, EnumSet.of(VisitHint.SKIP_ITERATION));
        root.visitTree(visit, (ctx, component) -> {
            if (id.equals(component.getId())) {
                result[0] = component;
                return VisitResult.COMPLETE;
            }
            return VisitResult.ACCEPT;
        });
        return result[0];
    }
}
