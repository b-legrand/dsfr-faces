package fr.gouv.dsfr.faces.richfaces;

import fr.gouv.dsfr.faces.render.DsfrRendererBase;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Components sending their own ajax request from JavaScript ({@code a4j:jsFunction}, {@code a4j:poll}),
 * with {@code faces.ajax.request(element, null, options)} and their element as source. Decode: the action
 * is queued when the component is the source of the request.
 */
abstract class A4jRequestRendererBase extends DsfrRendererBase {

    private static final Map<String, String> CALLBACK_STATUS = Map.of(
            "onbegin", "begin", "onbeforedomupdate", "complete", "oncomplete", "success");

    @Override
    public void decode(FacesContext context, UIComponent component) {
        if (!bool(component, "disabled") && isSubmitted(context, component.getClientId(context))) {
            component.queueEvent(new ActionEvent(component));
        }
    }

    /**
     * Options object of {@code faces.ajax.request}: execute (always including the component, so that its
     * action runs), render, onevent (status, onbegin, onbeforedomupdate, oncomplete), onerror, params.
     *
     * @param defaultExecute {@link AjaxIds#REGION_OR_FORM} or {@link AjaxIds#REGION_OR_THIS}
     * @param paramValue     JavaScript value of each {@code a4j:param} child, from its index and the parameter
     */
    protected static String options(FacesContext context, UIComponent component, String defaultExecute,
                                    ParamValue paramValue) {
        List<String> execute = new ArrayList<>(AjaxIds.forRequest(context, component,
                executeIds(component, defaultExecute)));
        if (!execute.contains("@this") && !execute.contains("@form") && !execute.contains("@all")) {
            execute.add("@this");
        }
        StringBuilder js = new StringBuilder("{execute:").append(jsString(String.join(" ", execute)));
        List<String> render = AjaxIds.forRequest(context, component,
                AjaxIds.split(component.getAttributes().get("render")));
        if (!render.isEmpty()) {
            js.append(",render:").append(jsString(String.join(" ", render)));
        }
        String onevent = onevent(component);
        if (onevent != null) {
            js.append(",onevent:").append(onevent);
        }
        String onerror = str(component, "onerror");
        if (!isBlank(onerror)) {
            js.append(",onerror:function(data){").append(onerror).append("\n}");
        }
        StringBuilder params = new StringBuilder();
        int index = 0;
        for (UIComponent child : component.getChildren()) {
            if (child instanceof A4jParam param && !param.isDisable() && !isBlank(param.getName())) {
                params.append(params.isEmpty() ? "" : ",").append(jsString(param.getName())).append(':')
                        .append(paramValue.of(index++, param));
            }
        }
        if (!params.isEmpty()) {
            js.append(",params:{").append(params).append('}');
        }
        if (bool(component, "resetValues")) {
            js.append(",resetValues:true");
        }
        String delay = str(component, "delay");
        if (!isBlank(delay)) {
            js.append(",delay:").append(jsString(delay));
        }
        return js.append('}').toString();
    }

    /** Value of an {@code a4j:param} as a string literal, or the expression itself with {@code noEscape}. */
    protected static String literalValue(A4jParam param) {
        Object value = param.getValue();
        if (bool(param, "noEscape")) {
            return value == null ? "''" : "(" + value + ")";
        }
        return jsString(value == null ? "" : value.toString());
    }

    private static List<String> executeIds(UIComponent component, String defaultExecute) {
        List<String> ids = AjaxIds.split(component.getAttributes().get("execute"));
        return ids.isEmpty() ? List.of(defaultExecute) : ids;
    }

    private static String onevent(UIComponent component) {
        StringBuilder js = new StringBuilder();
        String status = str(component, "status");
        if (!isBlank(status)) {
            js.append("if(data.status==='begin'&&data.source){data.source.dsfrA4jStatus=")
                    .append(jsString(status)).append("}");
        }
        for (String callback : List.of("onbegin", "onbeforedomupdate", "oncomplete")) {
            String script = str(component, callback);
            if (!isBlank(script)) {
                js.append("if(data.status==='").append(CALLBACK_STATUS.get(callback)).append("'){")
                        .append(script).append("\n}");
            }
        }
        return js.isEmpty() ? null : "function(data){" + js + "}";
    }

    @FunctionalInterface
    protected interface ParamValue {
        String of(int index, A4jParam param);
    }
}
