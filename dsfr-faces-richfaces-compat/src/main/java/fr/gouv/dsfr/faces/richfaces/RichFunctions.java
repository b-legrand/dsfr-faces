package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.util.Components;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.util.Arrays;
import java.util.Collection;

/**
 * RichFaces EL functions ({@code #{rich:clientId('id')}}…), declared in {@code META-INF/rich.taglib.xml}.
 * After the sed they are written {@code #{dsfr:clientId('id')}} and still resolve here, because the
 * {@code dsfr} prefix is bound to the {@code http://richfaces.org/rich} URI.
 * <p>
 * Ids are looked up as RichFaces did, from the component being processed: relative, then enclosing naming
 * containers, then the whole view. Functions return {@code null} (an empty string in EL) when not found.
 */
public final class RichFunctions {

    private RichFunctions() {
    }

    public static UIComponent findComponent(String id) {
        FacesContext context = FacesContext.getCurrentInstance();
        return Components.find(context, UIComponent.getCurrentComponent(context), id);
    }

    public static String clientId(String id) {
        UIComponent component = findComponent(id);
        return component == null ? null : component.getClientId(FacesContext.getCurrentInstance());
    }

    /** {@code document.getElementById('clientId')} */
    public static String element(String id) {
        String clientId = clientId(id);
        return clientId == null ? null : "document.getElementById('" + clientId + "')";
    }

    /** {@code RichFaces.component('clientId')}: the JavaScript API of RichFaces components. */
    public static String component(String id) {
        String clientId = clientId(id);
        return clientId == null ? null : "RichFaces.component('" + clientId + "')";
    }

    /** CSS selector of the component ({@code #clientId}, special characters escaped for a JS string). */
    public static String jQuerySelector(String id) {
        String clientId = clientId(id);
        if (clientId == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("#");
        for (char c : clientId.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '_') {
                sb.append("\\\\");
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** True if the user has one of the roles: a collection, an array, or a comma separated string. */
    public static boolean isUserInRole(Object roles) {
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        Collection<?> list;
        if (roles instanceof Collection<?> c) {
            list = c;
        } else if (roles instanceof Object[] array) {
            list = Arrays.asList(array);
        } else if (roles == null) {
            return false;
        } else {
            list = Arrays.asList(roles.toString().split("[\\s,]+"));
        }
        return list.stream().filter(r -> r != null && !r.toString().isBlank())
                .anyMatch(r -> external.isUserInRole(r.toString().trim()));
    }
}
