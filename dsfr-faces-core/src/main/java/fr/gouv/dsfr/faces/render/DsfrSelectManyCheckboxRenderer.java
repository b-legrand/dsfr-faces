package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrSelectManyCheckbox;
import fr.gouv.dsfr.faces.util.SelectItems;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.model.SelectItem;
import jakarta.faces.render.FacesRenderer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * DSFR checkboxes (see {@link DsfrChoicesRendererBase}). The value can be an array, a {@code List}, a
 * {@code Set} or a {@code SortedSet}; elements are converted with the explicit converter, or by the type of
 * the array elements, or of the item values.
 */
@FacesRenderer(componentFamily = UISelectMany.COMPONENT_FAMILY, rendererType = DsfrSelectManyCheckbox.RENDERER_TYPE)
public class DsfrSelectManyCheckboxRenderer extends DsfrChoicesRendererBase {

    @Override
    protected String inputType() {
        return "checkbox";
    }

    /** Nothing checked is not submitted: absent means an empty selection. */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        UIInput input = (UIInput) component;
        if (isNotSubmittable(input)) {
            return;
        }
        decodeBehaviors(context, component);
        String[] values = context.getExternalContext().getRequestParameterValuesMap().get(component.getClientId(context));
        input.setSubmittedValue(values == null ? new String[0] : values);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
            throws ConverterException {
        UIInput input = (UIInput) component;
        String[] values = (String[]) submittedValue;
        ValueExpression ve = input.getValueExpression("value");
        Class<?> type = ve == null ? null : ve.getType(context.getELContext());

        Class<?> elementType = type != null && type.isArray() ? type.getComponentType() : itemType(context, input);
        Converter converter = input.getConverter();
        if (converter == null && elementType != null && elementType != String.class && elementType != Object.class) {
            converter = context.getApplication().createConverter(elementType);
        }
        List<Object> converted = new ArrayList<>(values.length);
        for (String value : values) {
            converted.add(converter == null ? value : converter.getAsObject(context, component, value));
        }

        if (type != null && type.isArray()) {
            Object array = Array.newInstance(type.getComponentType(), converted.size());
            for (int i = 0; i < converted.size(); i++) {
                Array.set(array, i, converted.get(i));
            }
            return array;
        }
        if (type != null && SortedSet.class.isAssignableFrom(type)) {
            return new TreeSet<>(converted);
        }
        if (type != null && Set.class.isAssignableFrom(type)) {
            return new LinkedHashSet<>(converted);
        }
        if (type != null && Collection.class.isAssignableFrom(type)) {
            return converted;
        }
        return converted.toArray();
    }

    private static Class<?> itemType(FacesContext context, UIInput input) {
        for (SelectItem item : SelectItems.collect(context, input)) {
            if (item.getValue() != null) {
                return item.getValue().getClass();
            }
        }
        return null;
    }

    @Override
    protected Set<String> selectedValues(FacesContext context, UIInput input) {
        Set<String> selected = new HashSet<>();
        Object submitted = input.getSubmittedValue();
        if (submitted instanceof String[] array) {
            selected.addAll(List.of(array));
            return selected;
        }
        Object value = input.getValue();
        if (value instanceof Collection<?> collection) {
            collection.forEach(v -> selected.add(DsfrFieldRendererBase.itemString(context, input, v)));
        } else if (value != null && value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                selected.add(DsfrFieldRendererBase.itemString(context, input, Array.get(value, i)));
            }
        }
        return selected;
    }
}
