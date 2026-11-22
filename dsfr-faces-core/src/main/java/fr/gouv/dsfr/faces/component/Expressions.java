package fr.gouv.dsfr.faces.component;

import jakarta.el.ValueExpression;
import jakarta.faces.component.StateHelper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/** State written back to the model when the property is bound to a writable expression (activeItem...). */
final class Expressions {

    private Expressions() {
    }

    /** Sets the model through the value expression if writable, otherwise the local state. */
    static void set(UIComponent component, StateHelper state, String property, Object value) {
        ValueExpression ve = component.getValueExpression(property);
        FacesContext context = FacesContext.getCurrentInstance();
        if (ve != null && !ve.isReadOnly(context.getELContext())) {
            ve.setValue(context.getELContext(), value);
            state.remove(property);
        } else {
            state.put(property, value);
        }
    }
}
