package fr.gouv.dsfr.faces.richfaces;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.AjaxBehaviorListener;

import java.io.Serializable;

/**
 * {@code listener="#{bean.method}"} of {@code a4j:ajax}: the method may take an
 * {@link AjaxBehaviorEvent} or no argument, as with RichFaces and {@code f:ajax}.
 */
public class MethodExpressionAjaxListener implements AjaxBehaviorListener, Serializable {

    private static final long serialVersionUID = 1L;

    private MethodExpression oneArg;
    private MethodExpression noArg;

    /** For state restoring. */
    public MethodExpressionAjaxListener() {
    }

    public MethodExpressionAjaxListener(MethodExpression oneArg, MethodExpression noArg) {
        this.oneArg = oneArg;
        this.noArg = noArg;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event) {
        ELContext el = FacesContext.getCurrentInstance().getELContext();
        try {
            oneArg.invoke(el, new Object[]{event});
        } catch (MethodNotFoundException e) {
            noArg.invoke(el, new Object[0]);
        }
    }
}
