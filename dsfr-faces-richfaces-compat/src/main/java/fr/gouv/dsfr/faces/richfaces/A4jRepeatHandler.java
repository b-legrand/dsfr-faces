package fr.gouv.dsfr.faces.richfaces;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * {@code a4j:repeat} is the {@code ui:repeat} of the Faces implementation (component type
 * {@code facelets.ui.Repeat}, in Mojarra and MyFaces), with the RichFaces attribute names:
 * {@code first} → {@code begin}, {@code rows} → {@code end} ({@link RepeatEndExpression}),
 * {@code iterationStatusVar} → {@code varStatus} (index, first, last, even, odd...). The standard
 * {@code begin} / {@code end} are used rather than {@code offset} / {@code size}, whose meaning differs
 * between Mojarra and MyFaces. {@code rowKeyVar}, {@code keepSaved} and {@code rowKeyConverter} are
 * accepted and ignored.
 */
public class A4jRepeatHandler extends ComponentHandler {

    private final TagAttribute value;
    private final TagAttribute first;
    private final TagAttribute rows;

    public A4jRepeatHandler(ComponentConfig config) {
        super(config);
        this.value = getAttribute("value");
        this.first = getAttribute("first");
        this.rows = getAttribute("rows");
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type)
                .alias("first", "begin")
                .alias("iterationStatusVar", "varStatus")
                .ignore("rows").ignore("rowKeyVar").ignore("keepSaved").ignore("rowKeyConverter");
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        if (rows != null) {
            c.setValueExpression("end", new RepeatEndExpression(
                    value == null ? null : value.getValueExpression(ctx, Object.class),
                    first == null ? null : first.getValueExpression(ctx, Integer.class),
                    rows.getValueExpression(ctx, Integer.class)));
        }
    }
}
