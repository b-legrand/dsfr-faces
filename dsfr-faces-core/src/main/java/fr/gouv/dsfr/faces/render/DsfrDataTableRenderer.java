package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrColumn;
import fr.gouv.dsfr.faces.component.DsfrDataScroller;
import fr.gouv.dsfr.faces.component.DsfrDataTable;
import fr.gouv.dsfr.faces.util.Components;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * DSFR table:
 * <pre>{@code
 * <div id="form-table" class="fr-table fr-table--sm"><div class="fr-table__wrapper"><div class="fr-table__container">
 *   <div class="fr-table__content"><table id="form-table-table">
 *     <caption>Titre</caption>
 *     <thead><tr><th scope="col" aria-sort="ascending"><div class="fr-cell--sort"><span class="fr-cell__title">Nom</span>
 *       <button type="submit" id="form-table-sort-0" name="form-table-sort-0" class="fr-btn--sort-asc fr-btn fr-btn--sm">Trier</button></div></th></tr></thead>
 *     <tbody><tr class="…"><td>…</td></tr></tbody>
 *     <tfoot>…</tfoot>
 *   </table></div>
 * </div></div></div>
 * }</pre>
 * Also used for {@code h:dataTable} by the dsfr-faces-html module (any {@link UIData}; sorting only with
 * {@link DsfrDataTable}). A sort click re-renders the table and its scrollers with ajax.
 */
@FacesRenderer(componentFamily = UIData.COMPONENT_FAMILY, rendererType = DsfrDataTable.RENDERER_TYPE)
public class DsfrDataTableRenderer extends DsfrRendererBase {

    static String sortButtonId(String tableId, int column) {
        return tableId + "-sort-" + column;
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        if (!(component instanceof DsfrDataTable table)) {
            return;
        }
        String id = table.getClientId(context);
        List<UIColumn> columns = columns(table);
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) instanceof DsfrColumn column && column.isSortable()
                    && isSubmitted(context, sortButtonId(id, i))) {
                table.toggleSort(column);
                return;
            }
        }
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        UIData table = (UIData) component;
        String id = table.getClientId(context);
        List<UIColumn> columns = columns(table);
        ResponseWriter w = context.getResponseWriter();

        String size = str(table, "size");
        w.startElement("div", null);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-table",
                "sm".equals(size) || "lg".equals(size) ? "fr-table--" + size : null,
                bool(table, "bordered") ? "fr-table--bordered" : null,
                bool(table, "noScroll") ? "fr-table--no-scroll" : null,
                str(table, "styleClass")), null);
        for (String cssClass : List.of("fr-table__wrapper", "fr-table__container", "fr-table__content")) {
            w.startElement("div", null);
            w.writeAttribute("class", cssClass, null);
        }
        w.startElement("table", table);
        w.writeAttribute("id", id + "-table", null);

        UIComponent captionFacet = table.getFacet("caption") != null ? table.getFacet("caption") : table.getFacet("header");
        String caption = str(table, "caption");
        if ((captionFacet != null && captionFacet.isRendered()) || !isBlank(caption)) {
            w.startElement("caption", null);
            if (captionFacet != null && captionFacet.isRendered()) {
                captionFacet.encodeAll(context);
            } else {
                w.writeText(caption, null);
            }
            w.endElement("caption");
        }

        writeHeader(context, table, id, columns);
        writeBody(context, table, columns);
        writeFooter(context, columns);

        w.endElement("table");
        for (int i = 0; i < 4; i++) {
            w.endElement("div");
        }
    }

    private static List<UIColumn> columns(UIData table) {
        List<UIColumn> columns = new ArrayList<>();
        for (UIComponent child : table.getChildren()) {
            if (child instanceof UIColumn column && column.isRendered()) {
                columns.add(column);
            }
        }
        return columns;
    }

    private static void writeHeader(FacesContext context, UIData table, String id, List<UIColumn> columns)
            throws IOException {
        if (columns.stream().noneMatch(c -> c.getHeader() != null || isSortable(c))) {
            return;
        }
        ResponseWriter w = context.getResponseWriter();
        String render = null;
        w.startElement("thead", null);
        w.startElement("tr", null);
        for (int i = 0; i < columns.size(); i++) {
            UIColumn column = columns.get(i);
            w.startElement("th", null);
            w.writeAttribute("scope", "col", null);
            attr(w, "class", str(column, "headerClass"));
            if (!isSortable(column)) {
                encodeFacet(context, column.getHeader());
                w.endElement("th");
                continue;
            }
            String order = ((DsfrColumn) column).getSortOrder();
            if (!DsfrColumn.UNSORTED.equals(order)) {
                w.writeAttribute("aria-sort", order, null);
            }
            w.startElement("div", null);
            w.writeAttribute("class", "fr-cell--sort", null);
            w.startElement("span", null);
            w.writeAttribute("class", "fr-cell__title", null);
            encodeFacet(context, column.getHeader());
            w.endElement("span");
            String buttonId = sortButtonId(id, i);
            w.startElement("button", null);
            w.writeAttribute("type", "submit", null);
            w.writeAttribute("id", buttonId, null);
            w.writeAttribute("name", buttonId, null);
            w.writeAttribute("class", switch (order) {
                case DsfrColumn.ASCENDING -> "fr-btn--sort-asc fr-btn fr-btn--sm";
                case DsfrColumn.DESCENDING -> "fr-btn--sort-desc fr-btn fr-btn--sm";
                default -> "fr-btn--sort fr-btn fr-btn--sm";
            }, null);
            if (render == null) {
                render = String.join(" ", renderTargets(context, table));
            }
            w.writeAttribute("onclick", ajaxRequest(id, render), null);
            w.writeText("Trier", null);
            w.endElement("button");
            w.endElement("div");
            w.endElement("th");
        }
        w.endElement("tr");
        w.endElement("thead");
    }

    private static boolean isSortable(UIColumn column) {
        return column instanceof DsfrColumn c && c.isSortable();
    }

    /** The table and the scrollers bound to it, re-rendered after a sort (the first page is shown again). */
    private static List<String> renderTargets(FacesContext context, UIData table) {
        List<String> ids = new ArrayList<>();
        ids.add(table.getClientId(context));
        VisitContext visit = VisitContext.createVisitContext(context, null,
                EnumSet.of(VisitHint.SKIP_UNRENDERED, VisitHint.SKIP_ITERATION));
        context.getViewRoot().visitTree(visit, (ctx, c) -> {
            if (c instanceof DsfrDataScroller scroller
                    && Components.find(context, scroller, str(scroller, "for")) == table) {
                ids.add(scroller.getClientId(context));
            }
            return VisitResult.ACCEPT;
        });
        return ids;
    }

    private static void writeBody(FacesContext context, UIData table, List<UIColumn> columns) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        String[] rowClasses = split(str(table, "rowClasses"));
        String[] columnClasses = split(str(table, "columnClasses"));
        w.startElement("tbody", null);
        int first = table.getFirst();
        int rows = table.getRows();
        int rendered = 0;
        for (int index = first; rows <= 0 || index < first + rows; index++) {
            table.setRowIndex(index);
            if (!table.isRowAvailable()) {
                break;
            }
            w.startElement("tr", null);
            if (rowClasses.length > 0) {
                w.writeAttribute("class", rowClasses[rendered % rowClasses.length], null);
            }
            for (int i = 0; i < columns.size(); i++) {
                UIColumn column = columns.get(i);
                w.startElement("td", null);
                attr(w, "class", classes(columnClasses.length > 0 ? columnClasses[i % columnClasses.length] : null,
                        str(column, "styleClass")));
                for (UIComponent child : column.getChildren()) {
                    if (child.isRendered()) {
                        child.encodeAll(context);
                    }
                }
                w.endElement("td");
            }
            w.endElement("tr");
            rendered++;
        }
        table.setRowIndex(-1);
        if (rendered == 0) {
            w.startElement("tr", null);
            w.startElement("td", null);
            w.writeAttribute("colspan", String.valueOf(Math.max(1, columns.size())), null);
            facetOrText(context, table, "noData", str(table, "noDataLabel"));
            w.endElement("td");
            w.endElement("tr");
        }
        w.endElement("tbody");
    }

    private static void writeFooter(FacesContext context, List<UIColumn> columns) throws IOException {
        if (columns.stream().noneMatch(c -> c.getFooter() != null && c.getFooter().isRendered())) {
            return;
        }
        ResponseWriter w = context.getResponseWriter();
        w.startElement("tfoot", null);
        w.startElement("tr", null);
        for (UIColumn column : columns) {
            w.startElement("td", null);
            encodeFacet(context, column.getFooter());
            w.endElement("td");
        }
        w.endElement("tr");
        w.endElement("tfoot");
    }

    private static void encodeFacet(FacesContext context, UIComponent facet) throws IOException {
        if (facet != null && facet.isRendered()) {
            facet.encodeAll(context);
        }
    }

    private static String[] split(String classes) {
        return isBlank(classes) ? new String[0] : classes.trim().split("\\s*,\\s*");
    }
}
