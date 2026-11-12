package fr.gouv.dsfr.faces;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Library-wide constants and helpers. */
public final class Dsfr {

    /** Facelets namespace: {@code xmlns:dsfr="http://gouv.fr/dsfr-faces"}. */
    public static final String NAMESPACE = "http://gouv.fr/dsfr-faces";

    /** Component family for components that do not extend a standard Faces component. */
    public static final String COMPONENT_FAMILY = "fr.gouv.dsfr";

    /**
     * Path, relative to the web application context, where dsfr-faces-resources exposes DSFR dist/
     * (served by the Servlet container from META-INF/resources, not by the Faces resource handler).
     */
    public static final String RESOURCE_PATH = "/dsfr";

    /** Version of the bundled DSFR, used as a cache-busting query parameter. */
    public static final String DSFR_VERSION;

    static {
        Properties props = new Properties();
        try (InputStream in = Dsfr.class.getResourceAsStream("dsfr-faces.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // fall back to "unknown"
        }
        DSFR_VERSION = props.getProperty("dsfr.version", "unknown");
    }

    private Dsfr() {
    }

    /** URL of a file from DSFR dist/, e.g. {@code resourceUrl(ctx, "dsfr.min.css")}. */
    public static String resourceUrl(FacesContext context, String path) {
        ExternalContext ext = context.getExternalContext();
        return ext.encodeResourceURL(ext.getRequestContextPath() + RESOURCE_PATH + "/" + path + "?v=" + DSFR_VERSION);
    }
}
