package fr.gouv.dsfr.faces.ajax;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.PartialViewContextWrapper;
import jakarta.faces.event.PhaseId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds "ajaxRendered" components to every ajax response, as RichFaces did: a component whose
 * {@code ajaxRendered} attribute (or property) is {@code true} is re-rendered by every ajax request,
 * without being listed in {@code render}. {@code dsfr:messages} and {@code dsfr:message} are
 * ajaxRendered by default; any other component can opt in with {@code ajaxRendered="true"}.
 * <p>
 * Skipped when the request renders {@code @all}, when the component is already inside a render
 * target, and when the source component carries {@code fr.gouv.dsfr.limitRender.<event>}
 * (RichFaces {@code limitRender="true"}).
 */
public class DsfrPartialViewContext extends PartialViewContextWrapper {

    /** Component attribute prefix, followed by the behavior event name (action, blur...). */
    public static final String LIMIT_RENDER_ATTRIBUTE_PREFIX = "fr.gouv.dsfr.limitRender.";

    private static final Logger LOG = Logger.getLogger(DsfrPartialViewContext.class.getName());

    private final FacesContext context;
    private boolean ajaxRenderedAdded;

    public DsfrPartialViewContext(PartialViewContext wrapped, FacesContext context) {
        super(wrapped);
        this.context = context;
    }

    @Override
    public void processPartial(PhaseId phaseId) {
        if (phaseId == PhaseId.RENDER_RESPONSE && !ajaxRenderedAdded) {
            ajaxRenderedAdded = true;
            if (isAjaxRequest() && !isRenderAll() && !isLimitRender()) {
                addAjaxRendered();
            }
        }
        super.processPartial(phaseId);
    }

    private boolean isLimitRender() {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String source = params.get(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME);
        String event = params.get(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME);
        UIViewRoot root = context.getViewRoot();
        if (source == null || event == null || root == null) {
            return false;
        }
        boolean[] limit = {false};
        root.invokeOnComponent(context, source, (ctx, component) ->
                limit[0] = Boolean.TRUE.equals(component.getAttributes().get(LIMIT_RENDER_ATTRIBUTE_PREFIX + event)));
        return limit[0];
    }

    private void addAjaxRendered() {
        UIViewRoot root = context.getViewRoot();
        if (root == null) {
            return;
        }
        Collection<String> renderIds = getRenderIds();
        List<String> targets = renderIds.stream().filter(id -> !id.startsWith("@")).toList();
        String separator = String.valueOf(UINamingContainer.getSeparatorChar(context));
        List<String> added = new ArrayList<>();

        VisitContext visit = VisitContext.createVisitContext(context, null,
                EnumSet.of(VisitHint.SKIP_UNRENDERED, VisitHint.SKIP_ITERATION));
        root.visitTree(visit, (ctx, component) -> {
            if (component == root) {
                return VisitResult.ACCEPT;
            }
            String clientId = component.getClientId(context);
            for (String target : targets) {
                if (clientId.equals(target) || clientId.startsWith(target + separator)) {
                    return VisitResult.REJECT; // already rendered with its render target
                }
            }
            if (isAjaxRendered(component)) {
                added.add(clientId);
                return VisitResult.REJECT; // children are rendered with it
            }
            return VisitResult.ACCEPT;
        });

        if (added.isEmpty()) {
            return;
        }
        try {
            renderIds.remove("@none");
            renderIds.addAll(added);
        } catch (UnsupportedOperationException e) {
            LOG.log(Level.WARNING, "ajaxRendered ignored: the Faces implementation returned an immutable"
                    + " PartialViewContext.getRenderIds()", e);
        }
    }

    private static boolean isAjaxRendered(UIComponent component) {
        Object value = component.getAttributes().get("ajaxRendered");
        return Boolean.TRUE.equals(value) || "true".equals(value);
    }
}
