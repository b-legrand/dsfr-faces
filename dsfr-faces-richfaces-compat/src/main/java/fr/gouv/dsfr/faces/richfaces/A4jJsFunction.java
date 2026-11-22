package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;

/**
 * {@code <a4j:jsFunction name="refresh" action="#{bean.load}" render="list"><a4j:param name="id"
 * assignTo="#{bean.id}"/></a4j:jsFunction>}: defines the global JavaScript function {@code refresh(id)}
 * that sends an ajax request running the action, after assigning its arguments to the {@code a4j:param}
 * children, in order. Must be inside a form. {@code execute} defaults to {@code @form} (the enclosing
 * {@code a4j:region} if any).
 */
@FacesComponent(A4jJsFunction.COMPONENT_TYPE)
@ResourceDependency(library = "jakarta.faces", name = "faces.js")
public class A4jJsFunction extends UICommand {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.richfaces.JsFunction";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.richfaces.JsFunction";

    public A4jJsFunction() {
        setRendererType(RENDERER_TYPE);
    }
}
