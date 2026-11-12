package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrMessages;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIMessages;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Renders each message as a DSFR alert:
 * <pre>{@code
 * <div class="fr-alert fr-alert--error"><h3 class="fr-alert__title">Summary</h3><p>Detail</p></div>
 * <div class="fr-alert fr-alert--info fr-alert--sm"><p>Text</p></div>
 * }</pre>
 * {@code role="alert"} is added on ajax responses only, so that screen readers announce new messages
 * without announcing every message on page load.
 */
@FacesRenderer(componentFamily = UIMessages.COMPONENT_FAMILY, rendererType = DsfrMessages.RENDERER_TYPE)
public class DsfrMessagesRenderer extends DsfrRendererBase {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        DsfrMessages messages = (DsfrMessages) component;
        ResponseWriter w = context.getResponseWriter();
        boolean ajax = context.getPartialViewContext().isAjaxRequest();

        w.startElement("div", messages);
        w.writeAttribute("id", messages.getClientId(context), null);
        attr(w, "class", messages.getStyleClass());

        for (FacesMessage message : messagesToRender(context, messages)) {
            if (!messages.isRedisplay() && message.isRendered()) {
                continue;
            }
            String summary = messages.isShowSummary() ? message.getSummary() : null;
            String detail = messages.isShowDetail() ? message.getDetail() : null;
            if (Objects.equals(summary, detail)) {
                detail = null;
            }
            boolean titled = !isBlank(summary) && !isBlank(detail);

            w.startElement("div", null);
            w.writeAttribute("class", classes("fr-alert", "fr-alert--" + severity(message),
                    titled ? null : "fr-alert--sm"), null);
            if (ajax) {
                w.writeAttribute("role", "alert", null);
            }
            if (titled) {
                w.startElement(messages.getTitleTag(), null);
                w.writeAttribute("class", "fr-alert__title", null);
                w.writeText(summary, null);
                w.endElement(messages.getTitleTag());
            }
            w.startElement("p", null);
            w.writeText(titled ? detail : (isBlank(summary) ? detail : summary), null);
            w.endElement("p");
            w.endElement("div");
            message.rendered();
        }
        w.endElement("div");
    }

    private static List<FacesMessage> messagesToRender(FacesContext context, DsfrMessages messages) {
        if (messages.isGlobalOnly()) {
            return context.getMessageList(null);
        }
        String forId = messages.getFor();
        if (!isBlank(forId)) {
            UIComponent target = messages.findComponent(forId);
            return target == null ? List.of() : context.getMessageList(target.getClientId(context));
        }
        return context.getMessageList();
    }
}
