package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;

/**
 * {@code a4j:commandButton} (and later {@code a4j:commandLink}): a dsfr-faces command component with an
 * ajax behavior on {@code action}. {@code execute} defaults to {@code @form}; {@code render},
 * {@code oncomplete}, {@code limitRender}… are taken from the tag. {@code action}, {@code actionListener},
 * {@code immediate}, {@code disabled} and {@code onclick} stay on the component.
 */
public class A4jCommandHandler extends ComponentHandler {

    public A4jCommandHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset rules = super.createMetaRuleset(type);
        for (String attribute : AjaxSupport.COMMAND_ATTRIBUTES) {
            rules.ignore(attribute);
        }
        return rules;
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        AjaxSupport.attach(ctx, tag, this::getAttribute, c, "action", "@form", false);
    }
}
