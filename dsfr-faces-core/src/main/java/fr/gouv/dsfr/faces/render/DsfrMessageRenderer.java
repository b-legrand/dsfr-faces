package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrMessage;
import fr.gouv.dsfr.faces.util.Components;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.List;

/**
 * Renders the messages of the {@code for} component:
 * <pre>{@code
 * <div id="form-loginMsg" class="fr-messages-group" aria-live="polite">
 *   <p class="fr-message fr-message--error" id="form-loginMsg-0">Message</p>
 * </div>
 * }</pre>
 */
@FacesRenderer(componentFamily = UIMessage.COMPONENT_FAMILY, rendererType = DsfrMessage.RENDERER_TYPE)
public class DsfrMessageRenderer extends DsfrRendererBase {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        DsfrMessage message = (DsfrMessage) component;
        UIComponent target = Components.find(context, message, message.getFor());
        List<FacesMessage> messages = target == null
                ? List.of()
                : context.getMessageList(target.getClientId(context)).stream()
                        .filter(m -> message.isRedisplay() || !m.isRendered())
                        .toList();
        writeMessagesGroup(context.getResponseWriter(), message, message.getClientId(context),
                message.getStyleClass(), messages);
    }
}
