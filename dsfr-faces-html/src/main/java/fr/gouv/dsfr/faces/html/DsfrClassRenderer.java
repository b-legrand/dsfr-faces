package fr.gouv.dsfr.faces.html;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.RendererWrapper;

import java.io.IOException;

/**
 * Wraps a standard renderer of the Faces implementation: the implementation still produces the HTML
 * (behaviors, pass-through attributes, ajax scripts...), a {@link DsfrClassWriter} adds the DSFR class
 * to its main element, and the {@link Decoration} (aria attributes, label suffix in a dsfr:field).
 */
final class DsfrClassRenderer extends RendererWrapper {

    private final DsfrClass dsfrClass;

    DsfrClassRenderer(Renderer wrapped, DsfrClass dsfrClass) {
        super(wrapped);
        this.dsfrClass = dsfrClass;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        encode(context, component, () -> super.encodeBegin(context, component));
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        encode(context, component, () -> super.encodeEnd(context, component));
    }

    private void encode(FacesContext context, UIComponent component, Encoding encoding) throws IOException {
        ResponseWriter original = context.getResponseWriter();
        DsfrClassWriter writer = new DsfrClassWriter(original, component,
                Decoration.of(context, component, dsfrClass));
        context.setResponseWriter(writer);
        try {
            encoding.run();
            writer.finish();
        } finally {
            context.setResponseWriter(original);
        }
    }

    @FunctionalInterface
    private interface Encoding {
        void run() throws IOException;
    }
}
