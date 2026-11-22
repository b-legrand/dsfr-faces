package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.component.DsfrHead;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

import java.util.EnumSet;

/**
 * Adds the DSFR stylesheets and scripts ({@link DsfrHead}) to the {@code h:head} of every view that has
 * no {@code dsfr:head}, as RichFaces injected its own resources: migrated pages work without editing
 * their head. Registered for {@code PreRenderViewEvent} in {@code META-INF/faces-config.xml}.
 * <p>
 * Disable with the context parameter {@value #AUTO_HEAD_PARAM}{@code =false} (then add
 * {@code <dsfr:head/>} to your templates).
 * <p>
 * Also adds, in every case, {@code dsfr-faces/richfaces.js}: the {@code RichFaces.component(id)} object
 * used by {@code #{rich:component('id')}}.
 */
public class AutoHeadListener implements SystemEventListener {

    public static final String AUTO_HEAD_PARAM = "fr.gouv.dsfr.richfaces.AUTO_HEAD";

    static final String HEAD_ID = "dsfrAutoHead";
    static final String SCRIPT_ID = "dsfrRichFacesJs";

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof UIViewRoot;
    }

    @Override
    public void processEvent(SystemEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getPartialViewContext().isAjaxRequest() && !context.getPartialViewContext().isRenderAll()) {
            return; // the head is not rendered
        }
        UIViewRoot root = (UIViewRoot) event.getSource();
        addRichFacesScript(context, root);
        if ("false".equalsIgnoreCase(context.getExternalContext().getInitParameter(AUTO_HEAD_PARAM))
                || hasDsfrHead(context, root)) {
            return;
        }
        UIComponent head = context.getApplication().createComponent(DsfrHead.COMPONENT_TYPE);
        head.setId(HEAD_ID);
        root.addComponentResource(context, head, "head");
    }

    private static void addRichFacesScript(FacesContext context, UIViewRoot root) {
        for (UIComponent resource : root.getComponentResources(context, "head")) {
            if (SCRIPT_ID.equals(resource.getId())) {
                return;
            }
        }
        UIOutput script = new UIOutput();
        script.setId(SCRIPT_ID);
        script.setRendererType("jakarta.faces.resource.Script");
        script.getAttributes().put("library", "dsfr-faces");
        script.getAttributes().put("name", "richfaces.js");
        root.addComponentResource(context, script, "head");
    }

    /** Explicit dsfr:head anywhere in the view, or one added by a previous request (component resources are facets). */
    private static boolean hasDsfrHead(FacesContext context, UIViewRoot root) {
        boolean[] found = {false};
        VisitContext visit = VisitContext.createVisitContext(context, null, EnumSet.of(VisitHint.SKIP_ITERATION));
        root.visitTree(visit, (ctx, component) -> {
            if (component instanceof DsfrHead) {
                found[0] = true;
                return VisitResult.COMPLETE;
            }
            return VisitResult.ACCEPT;
        });
        return found[0];
    }
}
