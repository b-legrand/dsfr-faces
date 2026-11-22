package fr.gouv.dsfr.faces.ajax;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.PartialViewContextFactory;

/**
 * Registered in {@code META-INF/faces-config.xml}: wraps the implementation's partial view context
 * to support {@code ajaxRendered}.
 */
public class DsfrPartialViewContextFactory extends PartialViewContextFactory {

    public DsfrPartialViewContextFactory(PartialViewContextFactory wrapped) {
        super(wrapped);
    }

    @Override
    public PartialViewContext getPartialViewContext(FacesContext context) {
        return new DsfrPartialViewContext(getWrapped().getPartialViewContext(context), context);
    }
}
