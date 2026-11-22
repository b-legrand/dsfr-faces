package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrDataScroller;
import fr.gouv.dsfr.faces.util.Components;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;

/**
 * DSFR pagination of the {@code for} table:
 * <pre>{@code
 * <nav id="form-scroller" role="navigation" class="fr-pagination" aria-label="Pagination">
 *   <ul class="fr-pagination__list">
 *     <li><a class="fr-pagination__link fr-pagination__link--first" id="form-scroller-first" aria-disabled="true" role="link" title="Première page">Première page</a></li>
 *     <li><a class="fr-pagination__link fr-pagination__link--prev fr-pagination__link--lg-label" …>Page précédente</a></li>
 *     <li><a class="fr-pagination__link" aria-current="page" title="Page 1">1</a></li>
 *     <li><a class="fr-pagination__link" href="#" id="form-scroller-2" title="Page 2" onclick="…">2</a></li>
 *     …
 *   </ul>
 * </nav>
 * }</pre>
 * Links send an ajax request whose source id is {@code {scroller}-{first|prev|next|last|N}}, re-rendering the
 * table and the scroller. The table must be in a form.
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrDataScroller.RENDERER_TYPE)
public class DsfrDataScrollerRenderer extends DsfrRendererBase {

    private record Paging(UIData table, int rows, int pages, int current) {
    }

    private static Paging paging(FacesContext context, UIComponent scroller) {
        UIComponent target = Components.find(context, scroller, str(scroller, "for"));
        if (!(target instanceof UIData table) || table.getRows() <= 0) {
            return null;
        }
        int rows = table.getRows();
        int count = table.getRowCount();
        int pages = Math.max(1, (count + rows - 1) / rows);
        int current = Math.min(pages, table.getFirst() / rows + 1);
        return new Paging(table, rows, pages, current);
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Paging paging = paging(context, component);
        if (paging == null) {
            return;
        }
        String id = component.getClientId(context);
        int page = -1;
        if (isSubmitted(context, id + "-first")) {
            page = 1;
        } else if (isSubmitted(context, id + "-prev")) {
            page = paging.current() - 1;
        } else if (isSubmitted(context, id + "-next")) {
            page = paging.current() + 1;
        } else if (isSubmitted(context, id + "-last")) {
            page = paging.pages();
        } else {
            for (int p = 1; p <= paging.pages(); p++) {
                if (isSubmitted(context, id + "-" + p)) {
                    page = p;
                    break;
                }
            }
        }
        if (page < 1) {
            return;
        }
        page = Math.min(page, paging.pages());
        paging.table().setFirst((page - 1) * paging.rows());
        ValueExpression ve = component.getValueExpression("page");
        if (ve != null && !ve.isReadOnly(context.getELContext())) {
            ve.setValue(context.getELContext(), page);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        String id = component.getClientId(context);
        ResponseWriter w = context.getResponseWriter();
        w.startElement("nav", component);
        w.writeAttribute("id", id, null);
        w.writeAttribute("role", "navigation", null);
        w.writeAttribute("class", classes("fr-pagination", str(component, "styleClass")), null);
        w.writeAttribute("aria-label", "Pagination", null);
        w.startElement("ul", null);
        w.writeAttribute("class", "fr-pagination__list", null);

        Paging paging = paging(context, component);
        if (paging != null) {
            String render = paging.table().getClientId(context) + " " + id;
            int current = paging.current();
            int pages = paging.pages();
            boolean boundary = !"hide".equals(str(component, "boundaryControls"));
            boolean step = !"hide".equals(str(component, "stepControls"));
            if (boundary) {
                link(w, id, "first", "fr-pagination__link--first", "Première page", "Première page", current > 1, render);
            }
            if (step) {
                link(w, id, "prev", "fr-pagination__link--prev fr-pagination__link--lg-label", "Page précédente",
                        "Page précédente", current > 1, render);
            }
            int max = maxPages(component);
            int start = Math.max(1, Math.min(current - max / 2, pages - max + 1));
            int end = Math.min(pages, start + max - 1);
            if (start > 1) {
                ellipsis(w);
            }
            for (int p = start; p <= end; p++) {
                if (p == current) {
                    w.startElement("li", null);
                    w.startElement("a", null);
                    w.writeAttribute("class", "fr-pagination__link", null);
                    w.writeAttribute("id", id + "-" + p, null);
                    w.writeAttribute("aria-current", "page", null);
                    w.writeAttribute("title", "Page " + p, null);
                    w.writeText(String.valueOf(p), null);
                    w.endElement("a");
                    w.endElement("li");
                } else {
                    link(w, id, String.valueOf(p), null, "Page " + p, String.valueOf(p), true, render);
                }
            }
            if (end < pages) {
                ellipsis(w);
            }
            if (step) {
                link(w, id, "next", "fr-pagination__link--next fr-pagination__link--lg-label", "Page suivante",
                        "Page suivante", current < pages, render);
            }
            if (boundary) {
                link(w, id, "last", "fr-pagination__link--last", "Dernière page", "Dernière page", current < pages, render);
            }
        }
        w.endElement("ul");
        w.endElement("nav");
    }

    private static int maxPages(UIComponent component) {
        try {
            String max = str(component, "maxPages");
            return max == null ? 10 : Math.max(1, Integer.parseInt(max));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private static void link(ResponseWriter w, String scrollerId, String suffix, String modifier, String title,
                             String text, boolean enabled, String render) throws IOException {
        String linkId = scrollerId + "-" + suffix;
        w.startElement("li", null);
        w.startElement("a", null);
        w.writeAttribute("class", classes("fr-pagination__link", modifier), null);
        w.writeAttribute("id", linkId, null);
        w.writeAttribute("title", title, null);
        if (enabled) {
            w.writeAttribute("href", "#", null);
            w.writeAttribute("onclick", ajaxRequest(scrollerId, render), null);
        } else {
            w.writeAttribute("aria-disabled", "true", null);
            w.writeAttribute("role", "link", null);
        }
        w.writeText(text, null);
        w.endElement("a");
        w.endElement("li");
    }

    private static void ellipsis(ResponseWriter w) throws IOException {
        w.startElement("li", null);
        w.startElement("span", null);
        w.writeAttribute("class", "fr-pagination__link", null);
        w.writeText("…", null);
        w.endElement("span");
        w.endElement("li");
    }
}
