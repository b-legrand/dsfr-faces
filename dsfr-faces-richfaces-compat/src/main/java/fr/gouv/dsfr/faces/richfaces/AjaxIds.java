package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.util.Components;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * RichFaces resolution of {@code execute} / {@code render} ids, shared by the ajax behavior
 * ({@link RichAjaxBehavior}) and the components that write their own request ({@code a4j:jsFunction},
 * {@code a4j:poll}).
 * <ul>
 *     <li>{@code @region}: the enclosing {@code a4j:region}, or the whole view ({@code @all}) without one;</li>
 *     <li>defaults: inside an {@code a4j:region}, every a4j control executes the region (RichFaces); outside,
 *     {@code @form} for commands and {@code @this} for {@code a4j:ajax} and {@code a4j:poll};</li>
 *     <li>ids: relative lookup, then enclosing naming containers, then the whole view ({@link Components#find}).</li>
 * </ul>
 */
final class AjaxIds {

    /** Internal default execute: the enclosing region, or {@code @form}. Resolved when the script is rendered. */
    static final String REGION_OR_FORM = "@dsfr-region-or-form";
    /** Internal default execute: the enclosing region, or {@code @this}. */
    static final String REGION_OR_THIS = "@dsfr-region-or-this";

    private AjaxIds() {
    }

    /** Space or comma separated ids, or a collection / array of ids (EL value). */
    static List<String> split(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().filter(v -> v != null).flatMap(v -> split(v).stream()).toList();
        }
        if (value instanceof Object[] array) {
            return split(Arrays.asList(array));
        }
        return Arrays.stream(value.toString().trim().split("[\\s,]+")).filter(s -> !s.isEmpty()).toList();
    }

    /** Closest enclosing {@code a4j:region}, or {@code null}. */
    static A4jRegion region(UIComponent from) {
        for (UIComponent c = from; c != null; c = c.getParent()) {
            if (c instanceof A4jRegion region) {
                return region;
            }
        }
        return null;
    }

    /**
     * Ids for a behavior script, rendered by the Faces implementation: keywords and ids that the standard
     * relative lookup finds are kept, other ids become absolute ({@code :form-x}).
     */
    static List<String> forBehavior(FacesContext context, UIComponent from, Collection<String> ids) {
        char separator = UINamingContainer.getSeparatorChar(context);
        List<String> resolved = new ArrayList<>(ids.size());
        for (String id : ids) {
            String keyword = keyword(context, from, id);
            if (keyword != null) {
                resolved.add(keyword.charAt(0) == '@' ? keyword : separator + keyword);
            } else if (id.isEmpty() || id.charAt(0) == '@' || id.charAt(0) == separator || findsRelative(from, id)) {
                resolved.add(id);
            } else {
                UIComponent target = Components.find(context, from, id);
                // unknown ids are kept: the Faces implementation reports them
                resolved.add(target == null ? id : separator + target.getClientId(context));
            }
        }
        return resolved;
    }

    /**
     * Ids for a request written by dsfr-faces ({@code faces.ajax.request}): client ids, and the keywords that
     * faces.js understands ({@code @this @form @all @none}).
     */
    static List<String> forRequest(FacesContext context, UIComponent from, Collection<String> ids) {
        char separator = UINamingContainer.getSeparatorChar(context);
        List<String> resolved = new ArrayList<>(ids.size());
        for (String id : ids) {
            String keyword = keyword(context, from, id);
            if (keyword != null) {
                resolved.add(keyword);
            } else if (id.isEmpty() || id.charAt(0) == '@') {
                resolved.add(id);
            } else {
                String lookup = id.charAt(0) == separator ? id.substring(1) : id;
                UIComponent target = id.charAt(0) == separator
                        ? findAbsolute(context, lookup) : Components.find(context, from, lookup);
                resolved.add(target == null ? lookup : target.getClientId(context));
            }
        }
        return resolved;
    }

    /** Region keywords: the client id of the region, or the fallback keyword; {@code null} for other ids. */
    private static String keyword(FacesContext context, UIComponent from, String id) {
        String fallback = switch (id) {
            case "@region" -> "@all";
            case REGION_OR_FORM -> "@form";
            case REGION_OR_THIS -> "@this";
            default -> null;
        };
        if (fallback == null) {
            return null;
        }
        A4jRegion region = region(from);
        return region == null ? fallback : region.getClientId(context);
    }

    private static boolean findsRelative(UIComponent from, String id) {
        try {
            return from.findComponent(id) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static UIComponent findAbsolute(FacesContext context, String clientId) {
        UIComponent[] found = {null};
        context.getViewRoot().invokeOnComponent(context, clientId, (ctx, c) -> found[0] = c);
        return found[0];
    }
}
