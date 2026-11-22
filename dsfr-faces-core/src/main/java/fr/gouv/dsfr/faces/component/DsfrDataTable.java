package fr.gouv.dsfr.faces.component;

import jakarta.el.ValueExpression;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code <dsfr:dataTable>}: DSFR table ({@code fr-table}) of {@code dsfr:column} (or {@code h:column})
 * children. Replaces {@code rich:dataTable} and {@code h:dataTable}. Paginated with {@code rows} and a
 * {@code dsfr:dataScroller}; sortable columns (see {@link DsfrColumn}) are sorted here, on a copy of the
 * rows, the sorted order being stable across requests.
 * <p>
 * Attributes read by the renderer: caption (or facets caption / header), size (sm, md, lg), bordered,
 * noScroll, rowClasses, columnClasses, noDataLabel (or facet noData), styleClass.
 */
@ResourceDependency(library = "jakarta.faces", name = "faces.js")
@FacesComponent(DsfrDataTable.COMPONENT_TYPE)
public class DsfrDataTable extends UIData {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.DataTable";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.DataTableRenderer";

    private transient DataModel<?> sortedSource;
    private transient String sortedKey;
    private transient DataModel<?> sortedModel;

    public DsfrDataTable() {
        setRendererType(RENDERER_TYPE);
    }

    /** The column sorted on (single sort mode), or null. */
    public DsfrColumn getSortedColumn() {
        for (UIComponent child : getChildren()) {
            if (child instanceof DsfrColumn column && column.isRendered() && column.isSortable()
                    && !DsfrColumn.UNSORTED.equals(column.getSortOrder())) {
                return column;
            }
        }
        return null;
    }

    /** Next sort order of a column (ascending, then descending, then ascending...); other columns unsorted. */
    public void toggleSort(DsfrColumn sorted) {
        String next = DsfrColumn.ASCENDING.equals(sorted.getSortOrder()) ? DsfrColumn.DESCENDING : DsfrColumn.ASCENDING;
        for (UIComponent child : getChildren()) {
            if (child instanceof DsfrColumn column && column != sorted && column.isSortable()
                    && !DsfrColumn.UNSORTED.equals(column.getSortOrder())) {
                column.setSortOrder(DsfrColumn.UNSORTED);
            }
        }
        sorted.setSortOrder(next);
        setFirst(0);
        sortedModel = null;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        sortedModel = null; // the data may have changed during invoke application
        super.encodeBegin(context);
    }

    @Override
    protected DataModel getDataModel() {
        DataModel<?> model = super.getDataModel();
        DsfrColumn column = getSortedColumn();
        if (column == null) {
            return model;
        }
        // not the client id: inside the table it contains the current row index
        String key = column.getId() + ":" + column.getSortOrder();
        if (sortedModel != null && sortedSource == model && key.equals(sortedKey)) {
            return sortedModel;
        }
        sortedSource = model;
        sortedKey = key;
        sortedModel = new ListDataModel<>(sort(model, column));
        return sortedModel;
    }

    private List<Object> sort(DataModel<?> model, DsfrColumn column) {
        FacesContext context = FacesContext.getCurrentInstance();
        String var = getVar();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        Object previous = var == null ? null : requestMap.get(var);
        ValueExpression sortBy = column.getValueExpression("sortBy");

        List<Object[]> keyed = new ArrayList<>();
        int saved = model.getRowIndex();
        try {
            for (int i = 0; ; i++) {
                model.setRowIndex(i);
                if (!model.isRowAvailable()) {
                    break;
                }
                Object row = model.getRowData();
                if (var != null) {
                    requestMap.put(var, row);
                }
                keyed.add(new Object[]{sortBy.getValue(context.getELContext()), row});
            }
        } finally {
            model.setRowIndex(saved);
            if (var != null) {
                if (previous == null) {
                    requestMap.remove(var);
                } else {
                    requestMap.put(var, previous);
                }
            }
        }

        Locale locale = context.getViewRoot() == null ? Locale.FRENCH : context.getViewRoot().getLocale();
        Collator collator = Collator.getInstance(locale);
        Comparator<Object[]> comparator = (a, b) -> compare(a[0], b[0], collator);
        if (DsfrColumn.DESCENDING.equals(column.getSortOrder())) {
            comparator = comparator.reversed();
        }
        keyed.sort(comparator); // stable
        List<Object> rows = new ArrayList<>(keyed.size());
        keyed.forEach(k -> rows.add(k[1]));
        return rows;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compare(Object a, Object b, Collator collator) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a instanceof String sa && b instanceof String sb) {
            return collator.compare(sa, sb);
        }
        if (a instanceof Comparable ca && a.getClass().isInstance(b)) {
            return ca.compareTo(b);
        }
        return collator.compare(a.toString(), b.toString());
    }
}
