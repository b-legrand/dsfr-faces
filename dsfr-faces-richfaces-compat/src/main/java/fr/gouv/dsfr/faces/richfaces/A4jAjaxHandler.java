package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;
import jakarta.faces.view.facelets.TextHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * {@code <a4j:ajax event="…" execute="…" render="…" listener="…" oncomplete="…"/>}: standard Faces ajax with
 * RichFaces attributes. {@code execute} defaults to {@code @this} (the enclosing {@code a4j:region} if any),
 * {@code render} to nothing (ajaxRendered components such as {@code rich:messages} are still updated).
 * <ul>
 *     <li><b>nested</b> (no body): the behavior is added to the parent component;</li>
 *     <li><b>wrapping</b> (with a body): it is added to every component of the body that supports the event
 *     ({@code event} absent: the default event of each component). A component that already has an ajax
 *     behavior for that event (a nested {@code a4j:ajax}, an {@code a4j:commandButton}) keeps its own.</li>
 * </ul>
 */
public class A4jAjaxHandler extends TagHandler {

    /** Component attribute marking the components this tag already decorated (handlers can be re-applied). */
    private static final String WRAPPED_PREFIX = "fr.gouv.dsfr.a4j.wrapped.";

    private final TagAttribute event;
    private final boolean wrapping;

    public A4jAjaxHandler(TagConfig config) {
        super(config);
        this.event = getAttribute("event");
        // an empty tag has a no-op "leaf" next handler; a body is a component, text or tag handler, or several
        this.wrapping = nextHandler instanceof CompositeFaceletHandler || nextHandler instanceof TagHandler
                || nextHandler instanceof TextHandler;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (wrapping) {
            applyWrapping(ctx, parent);
            return;
        }
        if (parent == null || !ComponentHandler.isNew(parent)) {
            return; // behaviors are only added when the parent is created
        }
        AjaxSupport.attach(ctx, tag, this::getAttribute, parent, eventName(ctx), AjaxIds.REGION_OR_THIS, true);
    }

    private void applyWrapping(FaceletContext ctx, UIComponent parent) throws IOException {
        Set<UIComponent> before = Collections.newSetFromMap(new IdentityHashMap<>());
        before.addAll(parent.getChildren());
        nextHandler.apply(ctx, parent);
        String eventName = eventName(ctx);
        List<UIComponent> targets = new ArrayList<>();
        for (UIComponent child : parent.getChildren()) {
            if (!before.contains(child)) {
                collect(child, eventName, targets);
            }
        }
        String marker = WRAPPED_PREFIX + tagId;
        for (UIComponent target : targets) {
            if (target.getAttributes().containsKey(marker)) {
                continue;
            }
            target.getAttributes().put(marker, Boolean.TRUE);
            AjaxSupport.attach(ctx, tag, this::getAttribute, target, eventName, AjaxIds.REGION_OR_THIS, true);
        }
    }

    /** Components of the body supporting the event, without an ajax behavior of their own for it. */
    private static void collect(UIComponent component, String eventName, List<UIComponent> targets) {
        if (component instanceof ClientBehaviorHolder holder) {
            String name = eventName == null ? holder.getDefaultEventName() : eventName;
            if (name != null && holder.getEventNames().contains(name) && !hasAjax(holder, name)) {
                targets.add(component);
            }
        }
        for (UIComponent facet : component.getFacets().values()) {
            collect(facet, eventName, targets);
        }
        for (UIComponent child : component.getChildren()) {
            collect(child, eventName, targets);
        }
    }

    private static boolean hasAjax(ClientBehaviorHolder holder, String eventName) {
        List<ClientBehavior> behaviors = holder.getClientBehaviors().get(eventName);
        return behaviors != null && behaviors.stream().anyMatch(b -> b instanceof AjaxBehavior);
    }

    private String eventName(FaceletContext ctx) {
        if (event == null) {
            return null;
        }
        String value = event.getValue(ctx);
        return value == null || value.isBlank() ? null : value;
    }
}
