package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.component.DsfrSelectOneRadio;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.FacesRenderer;

import java.util.Set;

/** DSFR radio buttons (see {@link DsfrChoicesRendererBase}); {@code rich="true"}: rich radios. */
@FacesRenderer(componentFamily = UISelectOne.COMPONENT_FAMILY, rendererType = DsfrSelectOneRadio.RENDERER_TYPE)
public class DsfrSelectOneRadioRenderer extends DsfrChoicesRendererBase {

    /** No radio checked is not submitted: absent means empty, so that required is validated. */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        UIInput input = (UIInput) component;
        if (isNotSubmittable(input)) {
            return;
        }
        decodeBehaviors(context, component);
        String value = context.getExternalContext().getRequestParameterMap().get(component.getClientId(context));
        input.setSubmittedValue(value == null ? "" : value);
    }

    @Override
    protected String inputType() {
        return "radio";
    }

    @Override
    protected boolean isRich(UIInput input) {
        return bool(input, "rich");
    }

    @Override
    protected Set<String> selectedValues(FacesContext context, UIInput input) {
        return Set.of(currentValue(context, input));
    }
}
