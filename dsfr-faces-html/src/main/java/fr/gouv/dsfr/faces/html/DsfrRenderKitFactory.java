package fr.gouv.dsfr.faces.html;

import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

import java.util.Iterator;

/**
 * Registered in {@code META-INF/faces-config.xml}: returns the HTML_BASIC render kit wrapped by
 * {@link DsfrRenderKit}. Portable: only standard factory and wrapper APIs are used, the renderers of the
 * implementation (Mojarra, MyFaces) keep producing the HTML.
 * <p>
 * Disable with the context parameter {@value #ENABLED_PARAM}{@code =false}.
 */
public class DsfrRenderKitFactory extends RenderKitFactory {

    public static final String ENABLED_PARAM = "fr.gouv.dsfr.html.ENABLED";

    private volatile DsfrRenderKit htmlBasic;
    private volatile Boolean enabled;

    public DsfrRenderKitFactory(RenderKitFactory wrapped) {
        super(wrapped);
    }

    @Override
    public void addRenderKit(String renderKitId, RenderKit renderKit) {
        getWrapped().addRenderKit(renderKitId, renderKit);
    }

    @Override
    public RenderKit getRenderKit(FacesContext context, String renderKitId) {
        RenderKit renderKit = getWrapped().getRenderKit(context, renderKitId);
        if (renderKit == null || !HTML_BASIC_RENDER_KIT.equals(renderKitId) || !isEnabled(context)) {
            return renderKit;
        }
        DsfrRenderKit wrapper = htmlBasic;
        if (wrapper == null || wrapper.getWrapped() != renderKit) {
            wrapper = new DsfrRenderKit(renderKit);
            htmlBasic = wrapper;
        }
        return wrapper;
    }

    @Override
    public Iterator<String> getRenderKitIds() {
        return getWrapped().getRenderKitIds();
    }

    private boolean isEnabled(FacesContext context) {
        Boolean value = enabled;
        if (value == null) {
            FacesContext ctx = context != null ? context : FacesContext.getCurrentInstance();
            if (ctx == null) {
                return true; // during startup: do not cache
            }
            value = !"false".equalsIgnoreCase(ctx.getExternalContext().getInitParameter(ENABLED_PARAM));
            enabled = value;
        }
        return value;
    }
}
