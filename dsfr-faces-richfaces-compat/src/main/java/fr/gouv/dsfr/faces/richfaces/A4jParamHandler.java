package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;

/** {@code a4j:param}: adds the {@link A4jParamsListener} to the parent command, once. */
public class A4jParamHandler extends ComponentHandler {

    public A4jParamHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        if (!(parent instanceof ActionSource source)) {
            return; // e.g. in an a4j:poll without action: the value is still sent
        }
        for (ActionListener listener : source.getActionListeners()) {
            if (listener instanceof A4jParamsListener) {
                return;
            }
        }
        source.addActionListener(new A4jParamsListener());
    }
}
