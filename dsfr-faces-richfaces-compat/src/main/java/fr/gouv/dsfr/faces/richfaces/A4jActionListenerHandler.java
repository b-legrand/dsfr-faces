package fr.gouv.dsfr.faces.richfaces;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;

import java.io.Serializable;

/**
 * {@code <a4j:actionListener listener="#{bean.onAction}"/>} (method with an {@link ActionEvent} or no
 * argument), {@code type="com.example.MyListener"} or {@code binding="#{bean.listener}"}: adds an action
 * listener to the parent command, as {@code f:actionListener}, which has no {@code listener} attribute.
 */
public class A4jActionListenerHandler extends TagHandler {

    private final TagAttribute listener;
    private final TagAttribute type;
    private final TagAttribute binding;

    public A4jActionListenerHandler(TagConfig config) {
        super(config);
        this.listener = getAttribute("listener");
        this.type = getAttribute("type");
        this.binding = getAttribute("binding");
        if (listener == null && type == null && binding == null) {
            throw new TagException(tag, "one of listener, type or binding is required");
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) {
        if (!ComponentHandler.isNew(parent)) {
            return;
        }
        if (!(parent instanceof ActionSource source)) {
            throw new TagException(tag, "must be nested in a command component (ActionSource)");
        }
        if (listener != null) {
            MethodExpression oneArg = listener.getMethodExpression(ctx, null, new Class<?>[]{ActionEvent.class});
            MethodExpression noArg = listener.getMethodExpression(ctx, null, new Class<?>[0]);
            source.addActionListener(new MethodExpressionActionListener(oneArg, noArg));
        } else {
            source.addActionListener(new Delegating(
                    binding == null ? null : binding.getValueExpression(ctx, ActionListener.class),
                    type == null ? null : type.getValue(ctx)));
        }
    }

    /** {@code binding} (evaluated on each action), else a new instance of {@code type}. */
    static final class Delegating implements ActionListener, Serializable {

        private static final long serialVersionUID = 1L;

        private final ValueExpression binding;
        private final String type;

        Delegating(ValueExpression binding, String type) {
            this.binding = binding;
            this.type = type;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            var el = event.getFacesContext().getELContext();
            ActionListener delegate = binding == null ? null : (ActionListener) binding.getValue(el);
            if (delegate == null && type != null) {
                try {
                    delegate = (ActionListener) Class.forName(type, true,
                            Thread.currentThread().getContextClassLoader()).getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new AbortProcessingException("a4j:actionListener: cannot create " + type, e);
                }
                if (binding != null) {
                    binding.setValue(el, delegate);
                }
            }
            if (delegate != null) {
                delegate.processAction(event);
            }
        }
    }
}
