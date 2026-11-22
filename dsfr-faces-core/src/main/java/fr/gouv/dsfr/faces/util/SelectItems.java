package fr.gouv.dsfr.faces.util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Collects the {@link SelectItem}s of a select component from its {@code f:selectItem} and
 * {@code f:selectItems} children, as the standard renderers do:
 * {@code SelectItem} instances, arrays, collections, streams and maps (key = label, value = value), and
 * plain objects with {@code var} / {@code itemValue} / {@code itemLabel} / {@code itemDescription} /
 * {@code itemDisabled}. {@code SelectItemGroup}s are returned as is.
 */
public final class SelectItems {

    private SelectItems() {
    }

    public static List<SelectItem> collect(FacesContext context, UIComponent component) {
        List<SelectItem> items = new ArrayList<>();
        for (UIComponent child : component.getChildren()) {
            if (!child.isRendered()) {
                continue;
            }
            if (child instanceof UISelectItem item) {
                Object value = item.getValue();
                if (value instanceof SelectItem selectItem) {
                    items.add(selectItem);
                } else {
                    Object itemValue = item.getItemValue();
                    String label = item.getItemLabel();
                    SelectItem selectItem = new SelectItem(itemValue, label == null ? String.valueOf(itemValue) : label,
                            item.getItemDescription(), item.isItemDisabled(), item.isItemEscaped(),
                            item.isNoSelectionOption());
                    items.add(selectItem);
                }
            } else if (child instanceof UISelectItems selectItems) {
                addAll(context, selectItems, selectItems.getValue(), items);
            }
        }
        return items;
    }

    private static void addAll(FacesContext context, UISelectItems component, Object value, List<SelectItem> items) {
        if (value == null) {
            return;
        }
        if (value instanceof SelectItem item) {
            items.add(item);
        } else if (value instanceof Map<?, ?> map) {
            map.forEach((label, itemValue) -> items.add(new SelectItem(itemValue, String.valueOf(label))));
        } else if (value instanceof Object[] array) {
            addEach(context, component, Arrays.asList(array), items);
        } else if (value instanceof Collection<?> collection) {
            addEach(context, component, collection, items);
        } else if (value instanceof Stream<?> stream) {
            addEach(context, component, stream.toList(), items);
        } else if (value instanceof Iterable<?> iterable) {
            List<Object> list = new ArrayList<>();
            iterable.forEach(list::add);
            addEach(context, component, list, items);
        } else {
            addEach(context, component, List.of(value), items);
        }
    }

    private static void addEach(FacesContext context, UISelectItems component, Collection<?> values,
                                List<SelectItem> items) {
        String var = (String) component.getAttributes().get("var");
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        Object previous = var == null ? null : requestMap.get(var);
        try {
            for (Object value : values) {
                if (value instanceof SelectItem item) {
                    items.add(item);
                    continue;
                }
                if (var != null) {
                    requestMap.put(var, value);
                }
                Map<String, Object> attributes = component.getAttributes();
                Object itemValue = attributes.containsKey("itemValue") || component.getValueExpression("itemValue") != null
                        ? attributes.get("itemValue") : value;
                Object label = attributes.get("itemLabel");
                Object description = attributes.get("itemDescription");
                Object disabled = attributes.get("itemDisabled");
                items.add(new SelectItem(itemValue, label == null ? String.valueOf(itemValue) : label.toString(),
                        description == null ? null : description.toString(),
                        Boolean.TRUE.equals(disabled) || "true".equals(disabled)));
            }
        } finally {
            if (var != null) {
                if (previous == null) {
                    requestMap.remove(var);
                } else {
                    requestMap.put(var, previous);
                }
            }
        }
    }
}
