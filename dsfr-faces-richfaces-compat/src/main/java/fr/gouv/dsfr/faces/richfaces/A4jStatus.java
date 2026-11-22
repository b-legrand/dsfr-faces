package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * {@code <a4j:status name="…" startText="Chargement…" stopText="" errorText="Erreur"/>} (or facets
 * {@code start}, {@code stop}, {@code error}): shows the "start" content while an ajax request is running.
 * <p>
 * Without {@code name}, it follows every ajax request that does not name a status; with {@code name}, only the
 * requests whose source declares {@code status="name"} ({@code a4j:ajax}, {@code a4j:commandButton},
 * {@code a4j:jsFunction}...). Driven by {@code richfaces.js} ({@code faces.ajax.addOnEvent}).
 */
@FacesComponent(A4jStatus.COMPONENT_TYPE)
@ResourceDependency(library = "jakarta.faces", name = "faces.js")
public class A4jStatus extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.richfaces.Status";
    public static final String COMPONENT_FAMILY = "fr.gouv.dsfr.richfaces";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.richfaces.Status";

    public A4jStatus() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
}
