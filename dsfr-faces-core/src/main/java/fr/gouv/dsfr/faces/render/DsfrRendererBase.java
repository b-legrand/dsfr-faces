package fr.gouv.dsfr.faces.render;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helpers shared by all DSFR renderers.
 * <p>
 * Convention: only the element that should receive Faces pass-through attributes
 * ({@code xmlns:p="jakarta.faces.passthrough"}) is started with the component as second argument of
 * {@link ResponseWriter#startElement}; every other element is started with {@code null}. Both Mojarra
 * and MyFaces write pass-through attributes on elements started with their component.
 */
public abstract class DsfrRendererBase extends Renderer {

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) {
        // children (f:param, ...) are not rendered
    }

    /** Joins non-blank CSS classes. */
    protected static String classes(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(' ');
                }
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }

    /** Writes an attribute only when it has a non-empty value. */
    protected static void attr(ResponseWriter w, String name, Object value) throws IOException {
        if (value != null && !"".equals(value)) {
            w.writeAttribute(name, value, null);
        }
    }

    protected static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // ------------------------------------------------------------------ messages

    /** DSFR modifier for a Faces severity: error, warning or info. */
    protected static String severity(FacesMessage message) {
        FacesMessage.Severity s = message.getSeverity();
        if (s == null || s.compareTo(FacesMessage.SEVERITY_INFO) <= 0) {
            return "info";
        }
        if (s.compareTo(FacesMessage.SEVERITY_WARN) == 0) {
            return "warning";
        }
        return "error";
    }

    protected static boolean hasError(List<FacesMessage> messages) {
        return messages.stream().anyMatch(m -> "error".equals(severity(m)));
    }

    /** Detail if present, otherwise summary. */
    protected static String text(FacesMessage message) {
        return isBlank(message.getDetail()) ? message.getSummary() : message.getDetail();
    }

    // ------------------------------------------------------------------ client behaviors (f:ajax)

    /**
     * Queues behavior events (e.g. f:ajax listeners) when this component is the source of a behavior request.
     */
    protected static void decodeBehaviors(FacesContext context, UIComponent component) {
        if (!(component instanceof ClientBehaviorHolder holder) || holder.getClientBehaviors().isEmpty()) {
            return;
        }
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String event = params.get(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
        String source = params.get(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME);
        if (event == null || !component.getClientId(context).equals(source)) {
            return;
        }
        List<ClientBehavior> behaviors = holder.getClientBehaviors().get(event);
        if (behaviors != null) {
            for (ClientBehavior behavior : behaviors) {
                behavior.decode(context, component);
            }
        }
    }

    /**
     * Writes {@code on<event>} attributes for attached client behaviors.
     *
     * @param sourceId      id of the DOM element sent as {@code jakarta.faces.source}; must exist in the DOM
     *                      and equal the component clientId so that {@code @this} resolves server side
     * @param domEvents     logical event name to DOM event name (e.g. valueChange to change, action to click)
     * @param preventSubmit append {@code return false} when a behavior submits itself (ajax on a submit button)
     */
    protected static void renderBehaviors(FacesContext context, UIComponent component, String sourceId,
                                          Map<String, String> domEvents, boolean preventSubmit) throws IOException {
        if (!(component instanceof ClientBehaviorHolder holder) || holder.getClientBehaviors().isEmpty()) {
            return;
        }
        Map<String, List<String>> scripts = new LinkedHashMap<>();
        Set<String> submitting = new HashSet<>();
        for (Map.Entry<String, List<ClientBehavior>> entry : holder.getClientBehaviors().entrySet()) {
            String logical = entry.getKey();
            String dom = domEvents.getOrDefault(logical, logical);
            ClientBehaviorContext bc = ClientBehaviorContext.createClientBehaviorContext(
                    context, component, logical, sourceId, Collections.emptyList());
            for (ClientBehavior behavior : entry.getValue()) {
                String script = behavior.getScript(bc);
                if (!isBlank(script)) {
                    scripts.computeIfAbsent(dom, k -> new ArrayList<>()).add(script);
                    if (behavior.getHints().contains(ClientBehaviorHint.SUBMITTING)) {
                        submitting.add(dom);
                    }
                }
            }
        }
        ResponseWriter w = context.getResponseWriter();
        for (Map.Entry<String, List<String>> entry : scripts.entrySet()) {
            List<String> list = entry.getValue();
            String js = list.size() == 1
                    ? list.get(0)
                    : "faces.util.chain(this,event," + list.stream().map(DsfrRendererBase::jsString)
                            .collect(Collectors.joining(",")) + ")";
            if (preventSubmit && submitting.contains(entry.getKey())) {
                js += ";return false";
            }
            w.writeAttribute("on" + entry.getKey(), js, null);
        }
    }

    /** Single-quoted JavaScript string literal. */
    protected static String jsString(String s) {
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "") + "'";
    }
}
