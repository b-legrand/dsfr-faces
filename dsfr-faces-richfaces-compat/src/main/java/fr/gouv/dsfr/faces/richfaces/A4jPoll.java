package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;

/**
 * {@code <a4j:poll id="poll" interval="5000" enabled="#{bean.polling}" action="#{bean.tick}" render="x"/>}:
 * sends an ajax request every {@code interval} milliseconds (default 1000) while {@code enabled} (default
 * true). The next request is scheduled when the previous one has completed. Re-rendering the poll applies a
 * new {@code enabled} / {@code interval}; removing it from the page stops it. Must be inside a form.
 * {@code execute} defaults to {@code @this} (the enclosing {@code a4j:region} if any), so that polling
 * does not validate the form.
 */
@FacesComponent(A4jPoll.COMPONENT_TYPE)
@ResourceDependency(library = "jakarta.faces", name = "faces.js")
public class A4jPoll extends UICommand {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.richfaces.Poll";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.richfaces.Poll";

    public A4jPoll() {
        setRendererType(RENDERER_TYPE);
    }
}
