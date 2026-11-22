package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrCalendar;
import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * {@code <input type="date" class="fr-input">}. The browser always submits {@code yyyy-MM-dd} (whatever
 * the displayed format), converted to the type of the value: {@code LocalDate}, {@code LocalDateTime}
 * (start of day), {@code java.util.Date} (start of day in {@code timeZone}, default the server zone) or
 * {@code String} (formatted with {@code datePattern}, ISO otherwise). An explicit converter takes precedence.
 */
@FacesRenderer(componentFamily = UIInput.COMPONENT_FAMILY, rendererType = DsfrCalendar.RENDERER_TYPE)
public class DsfrCalendarRenderer extends DsfrFieldRendererBase {

    @Override
    protected String groupKind() {
        return "input";
    }

    @Override
    protected void encodeControl(FacesContext context, UIInput input, String controlId, String messagesId,
                                 String state) throws IOException {
        ResponseWriter w = context.getResponseWriter();
        w.startElement("input", input);
        w.writeAttribute("type", "date", null);
        writeControlAttributes(context, input, controlId, messagesId, state, "fr-input");
        w.writeAttribute("value", isoValue(context, input), "value");
        attr(w, "min", str(input, "min"));
        attr(w, "max", str(input, "max"));
        w.endElement("input");
    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
            throws ConverterException {
        UIInput input = (UIInput) component;
        if (input.getConverter() != null) {
            return super.getConvertedValue(context, component, submittedValue);
        }
        String submitted = (String) submittedValue;
        if (submitted == null || submitted.isBlank()) {
            return null;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(submitted.trim());
        } catch (DateTimeParseException e) {
            String label = str(input, "label");
            String summary = (label == null ? "" : label + " : ") + "la date doit être au format JJ/MM/AAAA.";
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, summary), e);
        }
        Class<?> type = valueType(context, input);
        if (type == LocalDateTime.class) {
            return date.atStartOfDay();
        }
        if (Date.class.isAssignableFrom(type)) {
            return Date.from(date.atStartOfDay(zone(input)).toInstant());
        }
        if (type == String.class) {
            String pattern = str(input, "datePattern");
            return pattern == null ? date.toString() : date.format(DateTimeFormatter.ofPattern(pattern));
        }
        return date;
    }

    /** yyyy-MM-dd for the value attribute (the submitted value after a failed validation). */
    private static String isoValue(FacesContext context, UIInput input) {
        Object submitted = input.getSubmittedValue();
        if (submitted != null) {
            return submitted.toString();
        }
        if (input.getConverter() != null) {
            return currentValue(context, input);
        }
        Object value = input.getValue();
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDate d) {
            return d.toString();
        }
        if (value instanceof LocalDateTime d) {
            return d.toLocalDate().toString();
        }
        if (value instanceof Date d) {
            return d.toInstant().atZone(zone(input)).toLocalDate().toString();
        }
        String pattern = str(input, "datePattern");
        if (value instanceof String s && pattern != null && !s.isBlank()) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern)).toString();
            } catch (DateTimeParseException e) {
                return "";
            }
        }
        return value.toString();
    }

    private static Class<?> valueType(FacesContext context, UIInput input) {
        ValueExpression ve = input.getValueExpression("value");
        Class<?> type = ve == null ? null : ve.getType(context.getELContext());
        return type == null ? Object.class : type;
    }

    private static ZoneId zone(UIInput input) {
        Object timeZone = input.getAttributes().get("timeZone");
        if (timeZone instanceof TimeZone tz) {
            return tz.toZoneId();
        }
        if (timeZone instanceof String s && !s.isBlank()) {
            return ZoneId.of(s);
        }
        return ZoneId.systemDefault();
    }
}
