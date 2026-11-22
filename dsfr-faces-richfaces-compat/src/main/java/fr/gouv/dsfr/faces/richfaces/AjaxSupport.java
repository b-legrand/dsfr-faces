package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.ajax.DsfrPartialViewContext;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Builds the {@link RichAjaxBehavior} of {@code a4j:ajax} and {@code a4j:commandButton} from RichFaces
 * tag attributes. Everything is delegated to standard Faces ajax ({@code faces.ajax.request}).
 */
final class AjaxSupport {

    /** Ajax attributes shared by a4j:ajax and the a4j command components. */
    static final Set<String> COMMAND_ATTRIBUTES = Set.of(
            "execute", "render", "onbegin", "onbeforedomupdate", "oncomplete", "onerror",
            "limitRender", "resetValues", "delay",
            // RichFaces-only, accepted and ignored for now
            "status", "bypassUpdates", "data", "focus", "requestGroupingId", "onbeforesubmit");

    /** RichFaces client event name to Faces ajax status (faces.ajax onevent). */
    private static final Map<String, String> EVENT_STATUS = new LinkedHashMap<>();

    static {
        EVENT_STATUS.put("onbegin", "begin");
        EVENT_STATUS.put("onbeforedomupdate", "complete");
        EVENT_STATUS.put("oncomplete", "success");
    }

    private AjaxSupport() {
    }

    /**
     * Creates the behavior and adds it to {@code parent} for {@code event}.
     *
     * @param event          behavior event, or {@code null} for the parent's default event
     * @param defaultExecute execute when the attribute is absent, or {@code null} (Faces default: @this)
     * @param behaviorOnly   also map {@code listener}, {@code disabled} and {@code immediate}
     *                       (a4j:ajax); on command components they belong to the component
     */
    static void attach(FaceletContext ctx, Tag tag, Function<String, TagAttribute> attributes,
                       UIComponent parent, String event, String defaultExecute, boolean behaviorOnly) {
        if (!(parent instanceof ClientBehaviorHolder holder)) {
            throw new TagException(tag, "must be nested in a component that supports client behaviors"
                    + " (ClientBehaviorHolder), e.g. h:inputText or dsfr:button");
        }
        String eventName = event == null || event.isBlank() ? holder.getDefaultEventName() : event;
        if (eventName == null || !holder.getEventNames().contains(eventName)) {
            throw new TagException(tag, "event \"" + eventName + "\" is not supported by " + parent.getClass().getName()
                    + "; supported events: " + holder.getEventNames());
        }

        RichAjaxBehavior behavior = new RichAjaxBehavior();
        ids(ctx, behavior, "execute", attributes.apply("execute"), defaultExecute);
        ids(ctx, behavior, "render", attributes.apply("render"), null);
        bool(ctx, behavior, "resetValues", attributes.apply("resetValues"));
        string(ctx, behavior, "delay", attributes.apply("delay"));
        string(ctx, behavior, "onerror", attributes.apply("onerror"));
        onevent(ctx, behavior, attributes);
        if (behaviorOnly) {
            bool(ctx, behavior, "disabled", attributes.apply("disabled"));
            bool(ctx, behavior, "immediate", attributes.apply("immediate"));
            TagAttribute listener = attributes.apply("listener");
            if (listener != null) {
                MethodExpression oneArg = listener.getMethodExpression(ctx, Object.class,
                        new Class<?>[]{AjaxBehaviorEvent.class});
                MethodExpression noArg = listener.getMethodExpression(ctx, Object.class, new Class<?>[0]);
                behavior.addAjaxBehaviorListener(new MethodExpressionAjaxListener(oneArg, noArg));
            }
        }
        TagAttribute limitRender = attributes.apply("limitRender");
        if (limitRender != null && limitRender.getBoolean(ctx)) {
            parent.getAttributes().put(DsfrPartialViewContext.LIMIT_RENDER_ATTRIBUTE_PREFIX + eventName, Boolean.TRUE);
        }

        holder.addClientBehavior(eventName, behavior);
        installFacesJs(ctx.getFacesContext());
    }

    /** execute / render: space or comma separated ids, keywords (@form...) or an EL expression. */
    private static void ids(FaceletContext ctx, RichAjaxBehavior behavior, String name, TagAttribute attribute,
                            String defaultValue) {
        if (attribute == null) {
            if (defaultValue != null) {
                set(behavior, name, List.of(defaultValue));
            }
        } else if (attribute.isLiteral()) {
            List<String> ids = Arrays.stream(attribute.getValue().trim().split("[\\s,]+"))
                    .filter(s -> !s.isEmpty()).toList();
            set(behavior, name, ids);
        } else {
            behavior.setValueExpression(name, attribute.getValueExpression(ctx, Object.class));
        }
    }

    private static void set(RichAjaxBehavior behavior, String name, List<String> ids) {
        if ("execute".equals(name)) {
            behavior.setExecute(ids);
        } else {
            behavior.setRender(ids);
        }
    }

    private static void bool(FaceletContext ctx, RichAjaxBehavior behavior, String name, TagAttribute attribute) {
        if (attribute == null) {
            return;
        }
        if (attribute.isLiteral()) {
            boolean value = Boolean.parseBoolean(attribute.getValue());
            switch (name) {
                case "disabled" -> behavior.setDisabled(value);
                case "immediate" -> behavior.setImmediate(value);
                case "resetValues" -> behavior.setResetValues(value);
                default -> throw new IllegalArgumentException(name);
            }
        } else {
            behavior.setValueExpression(name, attribute.getValueExpression(ctx, Boolean.class));
        }
    }

    private static void string(FaceletContext ctx, RichAjaxBehavior behavior, String name, TagAttribute attribute) {
        if (attribute == null) {
            return;
        }
        if (attribute.isLiteral()) {
            switch (name) {
                case "delay" -> behavior.setDelay(attribute.getValue());
                case "onerror" -> behavior.setOnerror(attribute.getValue());
                default -> throw new IllegalArgumentException(name);
            }
        } else {
            behavior.setValueExpression(name, attribute.getValueExpression(ctx, String.class));
        }
    }

    /**
     * onbegin / onbeforedomupdate / oncomplete become one faces.ajax {@code onevent} function, built as a
     * composite EL expression so that EL inside the scripts (e.g. {@code #{rich:element('x')}}) still works.
     */
    private static void onevent(FaceletContext ctx, RichAjaxBehavior behavior, Function<String, TagAttribute> attributes) {
        StringBuilder js = new StringBuilder();
        boolean literal = true;
        for (Map.Entry<String, String> entry : EVENT_STATUS.entrySet()) {
            TagAttribute attribute = attributes.apply(entry.getKey());
            if (attribute == null || attribute.getValue().isBlank()) {
                continue;
            }
            literal &= attribute.isLiteral();
            js.append("if(data.status==='").append(entry.getValue()).append("'){")
                    .append(attribute.getValue()).append("\n}");
        }
        if (js.isEmpty()) {
            return;
        }
        String function = "function(data){" + js + "}";
        if (literal) {
            behavior.setOnevent(function);
        } else {
            ValueExpression ve = ctx.getExpressionFactory().createValueExpression(ctx, function, String.class);
            behavior.setValueExpression("onevent", ve);
        }
    }

    /** Ensures faces.js is rendered in the head, as f:ajax does. */
    private static void installFacesJs(FacesContext context) {
        UIViewRoot root = context.getViewRoot();
        String name = ResourceHandler.FACES_SCRIPT_RESOURCE_NAME;
        String library = ResourceHandler.FACES_SCRIPT_LIBRARY_NAME;
        if (context.getApplication().getResourceHandler().isResourceRendered(context, name, library)) {
            return;
        }
        for (UIComponent resource : root.getComponentResources(context, "head")) {
            if (name.equals(resource.getAttributes().get("name"))
                    && library.equals(resource.getAttributes().get("library"))) {
                return;
            }
        }
        UIOutput script = new UIOutput();
        script.setRendererType("jakarta.faces.resource.Script");
        script.getAttributes().put("name", name);
        script.getAttributes().put("library", library);
        root.addComponentResource(context, script, "head");
    }
}
