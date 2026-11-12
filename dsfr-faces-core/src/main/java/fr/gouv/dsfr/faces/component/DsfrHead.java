package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Set;

/**
 * {@code <dsfr:head/>}: place inside {@code <h:head>}. Writes the DSFR stylesheets, favicons and scripts.
 * <p>
 * Scripts are loaded as {@code type="module"} (deferred by the browser) plus a {@code nomodule defer}
 * fallback, so they can live in the head. The DSFR JS watches DOM mutations, so markup inserted by
 * Faces ajax updates is initialised automatically.
 */
@FacesComponent(DsfrHead.COMPONENT_TYPE)
public class DsfrHead extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Head";

    private static final Set<String> SCHEMES = Set.of("system", "light", "dark");

    enum PropertyKeys { scheme, icons, favicon, themeColor }

    public DsfrHead() {
        setRendererType(null); // renders itself
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }

    /** Display scheme: system (default), light, dark, or empty to set {@code data-fr-scheme} on {@code <html>} yourself. */
    public String getScheme() {
        return (String) getStateHelper().eval(PropertyKeys.scheme, "system");
    }

    public void setScheme(String scheme) {
        getStateHelper().put(PropertyKeys.scheme, scheme);
    }

    /** Include the icon utility classes ({@code fr-icon-*}). Default true. */
    public boolean isIcons() {
        return (Boolean) getStateHelper().eval(PropertyKeys.icons, Boolean.TRUE);
    }

    public void setIcons(boolean icons) {
        getStateHelper().put(PropertyKeys.icons, icons);
    }

    /** Include the DSFR favicons and web manifest. Default true. */
    public boolean isFavicon() {
        return (Boolean) getStateHelper().eval(PropertyKeys.favicon, Boolean.TRUE);
    }

    public void setFavicon(boolean favicon) {
        getStateHelper().put(PropertyKeys.favicon, favicon);
    }

    /** Browser theme colour (Safari/Android). Default {@code #000091} (bleu France). */
    public String getThemeColor() {
        return (String) getStateHelper().eval(PropertyKeys.themeColor, "#000091");
    }

    public void setThemeColor(String themeColor) {
        getStateHelper().put(PropertyKeys.themeColor, themeColor);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        ResponseWriter w = context.getResponseWriter();

        String scheme = getScheme();
        if (scheme != null && !scheme.isBlank()) {
            if (!SCHEMES.contains(scheme)) {
                throw new IllegalArgumentException("dsfr:head scheme must be one of " + SCHEMES + ", got: " + scheme);
            }
            // Must run before DSFR JS initialises; value is whitelisted above, so raw write is safe.
            w.startElement("script", null);
            w.write("document.documentElement.setAttribute('data-fr-scheme','" + scheme + "');");
            w.endElement("script");
        }

        String themeColor = getThemeColor();
        if (themeColor != null && !themeColor.isBlank()) {
            w.startElement("meta", null);
            w.writeAttribute("name", "theme-color", null);
            w.writeAttribute("content", themeColor, null);
            w.endElement("meta");
        }

        if (isFavicon()) {
            link(context, w, "apple-touch-icon", "favicon/apple-touch-icon.png", null);
            link(context, w, "icon", "favicon/favicon.svg", "image/svg+xml");
            link(context, w, "shortcut icon", "favicon/favicon.ico", "image/x-icon");
            w.startElement("link", null);
            w.writeAttribute("rel", "manifest", null);
            w.writeURIAttribute("href", Dsfr.resourceUrl(context, "favicon/manifest.webmanifest"), null);
            w.writeAttribute("crossorigin", "use-credentials", null);
            w.endElement("link");
        }

        link(context, w, "stylesheet", "dsfr.min.css", null);
        if (isIcons()) {
            link(context, w, "stylesheet", "utility/icons/icons.min.css", null);
        }

        script(context, w, "dsfr.module.min.js", true);
        script(context, w, "legacy/legacy.nomodule.min.js", false);
        script(context, w, "dsfr.nomodule.min.js", false);
    }

    private static void link(FacesContext context, ResponseWriter w, String rel, String path, String type) throws IOException {
        w.startElement("link", null);
        w.writeAttribute("rel", rel, null);
        w.writeURIAttribute("href", Dsfr.resourceUrl(context, path), null);
        if (type != null) {
            w.writeAttribute("type", type, null);
        }
        w.endElement("link");
    }

    private static void script(FacesContext context, ResponseWriter w, String path, boolean module) throws IOException {
        w.startElement("script", null);
        if (module) {
            w.writeAttribute("type", "module", null);
        } else {
            w.writeAttribute("nomodule", "nomodule", null);
            w.writeAttribute("defer", "defer", null);
        }
        w.writeURIAttribute("src", Dsfr.resourceUrl(context, path), null);
        w.endElement("script");
    }
}
