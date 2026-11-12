package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrButton;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.Map;

/**
 * Renders {@code <button type="submit" class="fr-btn fr-btn--secondary fr-icon-… fr-btn--icon-left">Label</button>}.
 */
@FacesRenderer(componentFamily = UICommand.COMPONENT_FAMILY, rendererType = DsfrButton.RENDERER_TYPE)
public class DsfrButtonRenderer extends DsfrRendererBase {

    private static final Map<String, String> DOM_EVENTS = Map.of("action", "click");

    @Override
    public void decode(FacesContext context, UIComponent component) {
        DsfrButton button = (DsfrButton) component;
        if (button.isDisabled()) {
            return;
        }
        decodeBehaviors(context, component);

        String id = button.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String behaviorEvent = params.get(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
        boolean ajaxAction = id.equals(params.get(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME))
                && (behaviorEvent == null || "action".equals(behaviorEvent));
        if (params.containsKey(id) || ajaxAction) {
            button.queueEvent(new ActionEvent(button));
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        DsfrButton button = (DsfrButton) component;
        String id = button.getClientId(context);
        Object value = button.getValue();
        String label = value == null ? "" : value.toString();
        String icon = button.getIcon();
        String iconPosition = button.getIconPosition();
        boolean hasIcon = !isBlank(icon);
        boolean iconOnly = hasIcon && "only".equals(iconPosition);

        ResponseWriter w = context.getResponseWriter();
        w.startElement("button", button); // receives pass-through attributes
        w.writeAttribute("type", button.getType(), null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("name", id, null);
        w.writeAttribute("class", classes("fr-btn",
                priorityClass(button.getPriority()),
                sizeClass(button.getSize()),
                hasIcon ? icon : null,
                hasIcon && !iconOnly ? "fr-btn--icon-" + ("right".equals(iconPosition) ? "right" : "left") : null,
                button.getStyleClass()), null);
        String title = button.getTitle();
        attr(w, "title", isBlank(title) && iconOnly ? label : title);
        if (button.isDisabled()) {
            w.writeAttribute("disabled", "disabled", null);
        }
        renderBehaviors(context, button, id, DOM_EVENTS, true);
        w.writeText(label, null);
        w.endElement("button");
    }

    private static String priorityClass(String priority) {
        return switch (priority == null ? "primary" : priority) {
            case "secondary", "tertiary", "tertiary-no-outline" -> "fr-btn--" + priority;
            default -> null;
        };
    }

    private static String sizeClass(String size) {
        return switch (size == null ? "md" : size) {
            case "sm", "lg" -> "fr-btn--" + size;
            default -> null;
        };
    }
}
