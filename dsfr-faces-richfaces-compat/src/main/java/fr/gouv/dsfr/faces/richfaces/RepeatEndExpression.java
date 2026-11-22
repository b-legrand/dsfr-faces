package fr.gouv.dsfr.faces.richfaces;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.model.DataModel;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * {@code end} of {@code a4j:repeat} (inclusive index of {@code ui:repeat}), from the RichFaces
 * {@code first} and {@code rows}: {@code first + rows - 1}, limited to the last item (RichFaces accepts
 * {@code rows} larger than the list; Mojarra rejects an {@code end} beyond it). {@code null} (to the end) when
 * {@code rows} is absent or not positive.
 */
final class RepeatEndExpression extends ValueExpression {

    private static final long serialVersionUID = 1L;

    private final ValueExpression value;
    private final ValueExpression first;
    private final ValueExpression rows;

    RepeatEndExpression(ValueExpression value, ValueExpression first, ValueExpression rows) {
        this.value = value;
        this.first = first;
        this.rows = rows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(ELContext context) {
        int count = rows == null ? 0 : number(rows.getValue(context));
        if (count <= 0) {
            return null;
        }
        int start = first == null ? 0 : Math.max(number(first.getValue(context)), 0);
        int end = start + count - 1;
        int size = value == null ? -1 : size(value.getValue(context));
        if (size >= 0) {
            end = Math.min(end, Math.max(size - 1, start));
        }
        return (T) Integer.valueOf(end);
    }

    private static int number(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return value == null ? 0 : Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Number of items, or -1 if unknown. */
    private static int size(Object items) {
        if (items == null) {
            return 0;
        }
        if (items instanceof Collection<?> c) {
            return c.size();
        }
        if (items instanceof Map<?, ?> m) {
            return m.size();
        }
        if (items instanceof DataModel<?> m) {
            return m.getRowCount();
        }
        if (items.getClass().isArray()) {
            return Array.getLength(items);
        }
        if (items instanceof Iterable<?> iterable) {
            int n = 0;
            for (Object ignored : iterable) {
                n++;
            }
            return n;
        }
        return -1;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        throw new jakarta.el.PropertyNotWritableException("a4j:repeat end");
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        return true;
    }

    @Override
    public Class<?> getType(ELContext context) {
        return Integer.class;
    }

    @Override
    public Class<?> getExpectedType() {
        return Integer.class;
    }

    @Override
    public String getExpressionString() {
        return "a4j:repeat(first=" + (first == null ? "" : first.getExpressionString())
                + ", rows=" + (rows == null ? "" : rows.getExpressionString()) + ")";
    }

    @Override
    public boolean isLiteralText() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RepeatEndExpression other && Objects.equals(value, other.value)
                && Objects.equals(first, other.first) && Objects.equals(rows, other.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, first, rows);
    }
}
