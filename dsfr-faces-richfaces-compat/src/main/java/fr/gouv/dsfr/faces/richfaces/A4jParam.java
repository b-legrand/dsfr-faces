package fr.gouv.dsfr.faces.richfaces;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import java.util.Map;

/**
 * {@code <a4j:param name="id" value="#{item.id}" assignTo="#{bean.selectedId}"/>} in an a4j command,
 * {@code a4j:jsFunction} or {@code h:commandButton} / {@code h:commandLink}: a request parameter (as
 * {@code f:param}) whose submitted value is converted and written to {@code assignTo} before the action
 * runs (by {@link A4jParamsListener}).
 * <ul>
 *     <li>{@code noEscape="true"}: {@code value} is a JavaScript expression evaluated in the browser
 *     (e.g. {@code window.innerWidth});</li>
 *     <li>in {@code a4j:jsFunction}, the parameters are the arguments of the JavaScript function, in order;</li>
 *     <li>{@code converter}: a converter or converter id; otherwise the converter for the type of
 *     {@code assignTo}.</li>
 * </ul>
 */
@FacesComponent(A4jParam.COMPONENT_TYPE)
public class A4jParam extends UIParameter {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.richfaces.Param";

    /** Writes the submitted value (or, if none was submitted, {@code value}) to {@code assignTo}. */
    void assign(FacesContext context) {
        ValueExpression assignTo = getValueExpression("assignTo");
        if (assignTo == null || isDisable() || getName() == null) {
            return;
        }
        ELContext el = context.getELContext();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        Object value;
        if (params.containsKey(getName())) {
            String submitted = params.get(getName());
            Converter<Object> converter = converter(context, assignTo.getType(el));
            value = converter == null ? submitted : converter.getAsObject(context, this, submitted);
        } else if (Boolean.TRUE.equals(getAttributes().get("noEscape")) || "true".equals(getAttributes().get("noEscape"))) {
            return; // the value is client-side JavaScript
        } else {
            value = getValue();
        }
        assignTo.setValue(el, value);
    }

    @SuppressWarnings("unchecked")
    private Converter<Object> converter(FacesContext context, Class<?> type) {
        Object converter = getAttributes().get("converter");
        if (converter instanceof Converter<?> c) {
            return (Converter<Object>) c;
        }
        if (converter instanceof String id && !id.isBlank()) {
            return (Converter<Object>) context.getApplication().createConverter(id);
        }
        if (type == null || type == String.class || type == Object.class) {
            return null;
        }
        return (Converter<Object>) context.getApplication().createConverter(type);
    }
}
