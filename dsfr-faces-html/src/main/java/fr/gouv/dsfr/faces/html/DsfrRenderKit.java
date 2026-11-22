package fr.gouv.dsfr.faces.html;

import fr.gouv.dsfr.faces.render.DsfrMessageRenderer;
import fr.gouv.dsfr.faces.render.DsfrMessagesRenderer;
import jakarta.faces.component.UIMessage;
import jakarta.faces.component.UIMessages;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitWrapper;
import jakarta.faces.render.Renderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation's HTML_BASIC render kit, with:
 * <ul>
 *     <li>the renderers of {@link DsfrClass#BY_RENDERER} wrapped to add DSFR classes;</li>
 *     <li>{@code h:messages} and {@code h:message} rendered by the dsfr-faces renderers (alerts and
 *     inline messages), as {@code dsfr:messages} / {@code dsfr:message}.</li>
 * </ul>
 * Every other renderer is returned unchanged.
 */
final class DsfrRenderKit extends RenderKitWrapper {

    private static final String MESSAGES_TYPE = "jakarta.faces.Messages";
    private static final String MESSAGE_TYPE = "jakarta.faces.Message";

    private final Map<String, Renderer> renderers = new ConcurrentHashMap<>();

    DsfrRenderKit(RenderKit wrapped) {
        super(wrapped);
    }

    @Override
    public Renderer getRenderer(String family, String rendererType) {
        if (UIMessages.COMPONENT_FAMILY.equals(family) && MESSAGES_TYPE.equals(rendererType)) {
            return renderers.computeIfAbsent(key(family, rendererType), k -> new DsfrMessagesRenderer());
        }
        if (UIMessage.COMPONENT_FAMILY.equals(family) && MESSAGE_TYPE.equals(rendererType)) {
            return renderers.computeIfAbsent(key(family, rendererType), k -> new DsfrMessageRenderer());
        }
        DsfrClass dsfrClass = DsfrClass.of(family, rendererType);
        Renderer renderer = getWrapped().getRenderer(family, rendererType);
        if (dsfrClass == null || renderer == null) {
            return renderer;
        }
        return renderers.computeIfAbsent(key(family, rendererType), k -> new DsfrClassRenderer(renderer, dsfrClass));
    }

    @Override
    public void addRenderer(String family, String rendererType, Renderer renderer) {
        super.addRenderer(family, rendererType, renderer);
        renderers.remove(key(family, rendererType));
    }

    private static String key(String family, String rendererType) {
        return family + "|" + rendererType;
    }
}
