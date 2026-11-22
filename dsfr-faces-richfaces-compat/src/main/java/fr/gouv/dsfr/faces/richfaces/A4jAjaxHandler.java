package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import java.io.IOException;

/**
 * {@code <a4j:ajax event="…" execute="…" render="…" listener="…" oncomplete="…"/>} nested in a component:
 * standard Faces ajax with RichFaces attributes. {@code execute} defaults to {@code @this}, {@code render}
 * to nothing (ajaxRendered components such as {@code rich:messages} are still updated).
 * <p>
 * Not supported yet: the wrapping form ({@code <a4j:ajax>} around components).
 */
public class A4jAjaxHandler extends TagHandler {

    private final TagAttribute event;

    public A4jAjaxHandler(TagConfig config) {
        super(config);
        this.event = getAttribute("event");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent == null || !ComponentHandler.isNew(parent)) {
            return; // behaviors are only added when the parent is created
        }
        AjaxSupport.attach(ctx, tag, this::getAttribute, parent,
                event == null ? null : event.getValue(ctx), null, true);
    }
}
