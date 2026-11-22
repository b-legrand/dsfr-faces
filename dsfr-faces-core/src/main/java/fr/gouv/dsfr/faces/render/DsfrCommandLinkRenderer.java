package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrCommandLink;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code <a href="#" id="form-del" class="fr-link fr-icon-delete-line fr-link--icon-left" onclick="…">Supprimer</a>}.
 * Without ajax behavior, the click submits the enclosing form with a hidden field named after the client id
 * (and the {@code f:param} children). Disabled: no {@code href}, {@code aria-disabled="true"}.
 */
@FacesRenderer(componentFamily = UICommand.COMPONENT_FAMILY, rendererType = DsfrCommandLink.RENDERER_TYPE)
public class DsfrCommandLinkRenderer extends DsfrRendererBase {

    private static final Map<String, String> DOM_EVENTS = Map.of("action", "click");

    @Override
    public void decode(FacesContext context, UIComponent component) {
        if (bool(component, "disabled")) {
            return;
        }
        decodeBehaviors(context, component);
        String id = component.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String event = params.get(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
        boolean ajaxAction = id.equals(params.get(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME))
                && (event == null || "action".equals(event));
        if (params.containsKey(id) || ajaxAction) {
            component.queueEvent(new ActionEvent(component));
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        DsfrCommandLink link = (DsfrCommandLink) component;
        String id = link.getClientId(context);
        String icon = str(link, "icon");
        String size = str(link, "size");
        boolean disabled = bool(link, "disabled");
        ResponseWriter w = context.getResponseWriter();

        w.startElement("a", link);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-link",
                "sm".equals(size) || "lg".equals(size) ? "fr-link--" + size : null,
                icon,
                isBlank(icon) ? null : "fr-link--icon-" + ("right".equals(str(link, "iconPosition")) ? "right" : "left"),
                str(link, "styleClass")), null);
        attr(w, "title", str(link, "title"));
        if (disabled) {
            w.writeAttribute("aria-disabled", "true", null);
            w.writeAttribute("role", "link", null);
        } else {
            w.writeAttribute("href", "#", null);
            Map<String, String> user = new LinkedHashMap<>();
            String onclick = str(link, "onclick");
            String submit = hasActionBehavior(link) ? null : submitScript(context, link, id);
            String click = joinScripts(onclick, submit);
            if (click != null) {
                user.put("click", click);
            }
            renderBehaviors(context, link, id, DOM_EVENTS, true, user);
        }

        Object value = link.getValue();
        if (value != null) {
            w.writeText(value.toString(), null);
        } else {
            for (UIComponent child : link.getChildren()) {
                if (!(child instanceof UIParameter)) {
                    child.encodeAll(context);
                }
            }
        }
        w.endElement("a");
    }

    private static boolean hasActionBehavior(DsfrCommandLink link) {
        Map<String, List<ClientBehavior>> behaviors = link.getClientBehaviors();
        return behaviors.containsKey("action") || behaviors.containsKey("click");
    }

    /** User script then submit, in one handler: a user script returning false cancels the submit. */
    private static String joinScripts(String onclick, String submit) {
        if (isBlank(onclick)) {
            return submit;
        }
        if (submit == null) {
            return onclick;
        }
        return "if((function(event){" + onclick + "}).call(this,event)===false){return false;}" + submit;
    }

    private static String submitScript(FacesContext context, UIComponent link, String id) {
        StringBuilder js = new StringBuilder("var f=this.closest('form');if(f){");
        appendHidden(js, id, id);
        for (UIComponent child : link.getChildren()) {
            if (child instanceof UIParameter param && !param.isDisable() && param.getName() != null) {
                appendHidden(js, param.getName(), param.getValue() == null ? "" : param.getValue().toString());
            }
        }
        return js.append("f.submit();}return false").toString();
    }

    private static void appendHidden(StringBuilder js, String name, String value) {
        js.append("var i=document.createElement('input');i.type='hidden';i.name=").append(jsString(name))
                .append(";i.value=").append(jsString(value)).append(";f.appendChild(i);");
    }
}
