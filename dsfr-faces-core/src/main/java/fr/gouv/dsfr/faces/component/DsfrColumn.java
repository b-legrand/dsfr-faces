package fr.gouv.dsfr.faces.component;

import jakarta.el.ValueExpression;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIColumn;
import jakarta.faces.context.FacesContext;

/**
 * {@code <dsfr:column>}: column of a {@code dsfr:dataTable}, with {@code header} / {@code footer} facets.
 * Replaces {@code rich:column}.
 * <p>
 * Sorting, as RichFaces built-in sorting: {@code sortBy} is the value sorted on (an expression using the
 * table {@code var}), {@code sortOrder} is ascending, descending or unsorted. A sort button is rendered in
 * the header of a column with {@code sortBy}. {@code sortOrder} may be bound to the model (String or enum
 * with these names, case insensitive).
 * <p>
 * Attributes read by the renderer: styleClass, headerClass.
 */
@FacesComponent(DsfrColumn.COMPONENT_TYPE)
public class DsfrColumn extends UIColumn {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Column";

    public static final String ASCENDING = "ascending";
    public static final String DESCENDING = "descending";
    public static final String UNSORTED = "unsorted";

    public boolean isSortable() {
        return getValueExpression("sortBy") != null;
    }

    /** ascending, descending or unsorted. */
    public String getSortOrder() {
        Object value = getStateHelper().eval("sortOrder");
        if (value == null) {
            return UNSORTED;
        }
        String order = value.toString().toLowerCase();
        return ASCENDING.equals(order) || DESCENDING.equals(order) ? order : UNSORTED;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setSortOrder(String sortOrder) {
        ValueExpression ve = getValueExpression("sortOrder");
        FacesContext context = FacesContext.getCurrentInstance();
        if (ve != null && !ve.isReadOnly(context.getELContext())) {
            Class<?> type = ve.getType(context.getELContext());
            Object value = sortOrder;
            if (type != null && type.isEnum()) {
                for (Object constant : type.getEnumConstants()) {
                    if (((Enum) constant).name().equalsIgnoreCase(sortOrder)) {
                        value = constant;
                    }
                }
            }
            ve.setValue(context.getELContext(), value);
            getStateHelper().remove("sortOrder");
        } else {
            getStateHelper().put("sortOrder", sortOrder);
        }
    }
}
