package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

import java.io.Serializable;

/**
 * Added once to a command having {@code a4j:param} children: assigns them, in order, before the action
 * (action listeners run before the action). Stateless: it finds the parameters among the children.
 */
public class A4jParamsListener implements ActionListener, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void processAction(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        for (UIComponent child : event.getComponent().getChildren()) {
            if (child instanceof A4jParam param) {
                param.assign(context);
            }
        }
    }
}
