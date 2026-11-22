package fr.gouv.dsfr.faces.render;

import fr.gouv.dsfr.faces.Dsfr;
import fr.gouv.dsfr.faces.component.DsfrTab;
import fr.gouv.dsfr.faces.component.DsfrTabPanel;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.FacesRenderer;

import java.io.IOException;
import java.util.Objects;

/**
 * <pre>{@code
 * <div id="form-tabs" class="fr-tabs">
 *   <ul class="fr-tabs__list" role="tablist" aria-label="Onglets">
 *     <li role="presentation"><button type="button" id="form-tab1-tab" class="fr-tabs__tab" tabindex="0"
 *         role="tab" aria-selected="true" aria-controls="form-tab1">Onglet 1</button></li>
 *   </ul>
 *   <div id="form-tab1" class="fr-tabs__panel fr-tabs__panel--selected" role="tabpanel"
 *        aria-labelledby="form-tab1-tab" tabindex="0">…</div>
 * </div>
 * }</pre>
 * The panels are rendered by the tabs ({@link DsfrTabRenderer}).
 */
@FacesRenderer(componentFamily = Dsfr.COMPONENT_FAMILY, rendererType = DsfrTabPanel.RENDERER_TYPE)
public class DsfrTabPanelRenderer extends DsfrRendererBase {

    static String buttonId(FacesContext context, UIComponent tab) {
        return tab.getClientId(context) + "-tab";
    }

    /** Field added by the server switch script: the name of the clicked tab. */
    private static String switchParam(FacesContext context, UIComponent panel) {
        return panel.getClientId(context) + "-switch";
    }

    private static String hiddenId(FacesContext context, UIComponent panel) {
        return panel.getClientId(context) + "-active";
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        DsfrTabPanel panel = (DsfrTabPanel) component;
        if (panel.isClientSwitch()) {
            String active = context.getExternalContext().getRequestParameterMap().get(hiddenId(context, panel));
            if (active != null && !active.isEmpty() && !Objects.equals(active, panel.getActiveItem())) {
                panel.setActiveItem(active);
            }
            return;
        }
        String serverSwitch = context.getExternalContext().getRequestParameterMap().get(switchParam(context, panel));
        for (DsfrTab tab : panel.getTabs()) {
            boolean clicked = context.getPartialViewContext().isAjaxRequest()
                    ? isSubmitted(context, buttonId(context, tab))
                    : DsfrTabPanel.nameOf(tab).equals(serverSwitch);
            if (clicked) {
                panel.setActiveItem(DsfrTabPanel.nameOf(tab));
                panel.setSwitching(true);
                return;
            }
        }
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        DsfrTabPanel panel = (DsfrTabPanel) component;
        String id = panel.getClientId(context);
        DsfrTab active = panel.getActiveTab();
        ResponseWriter w = context.getResponseWriter();

        w.startElement("div", panel);
        w.writeAttribute("id", id, null);
        w.writeAttribute("class", classes("fr-tabs", str(panel, "styleClass")), null);
        if (panel.isClientSwitch()) {
            w.startElement("input", null);
            w.writeAttribute("type", "hidden", null);
            w.writeAttribute("id", hiddenId(context, panel), null);
            w.writeAttribute("name", hiddenId(context, panel), null);
            w.writeAttribute("value", active == null ? "" : DsfrTabPanel.nameOf(active), null);
            w.endElement("input");
        }

        w.startElement("ul", null);
        w.writeAttribute("class", "fr-tabs__list", null);
        w.writeAttribute("role", "tablist", null);
        String label = str(panel, "label");
        w.writeAttribute("aria-label", isBlank(label) ? "Onglets" : label, null);
        for (DsfrTab tab : panel.getTabs()) {
            boolean selected = tab == active;
            String icon = str(tab, "icon");
            String name = DsfrTabPanel.nameOf(tab);
            w.startElement("li", null);
            w.writeAttribute("role", "presentation", null);
            w.startElement("button", null);
            w.writeAttribute("type", "button", null);
            w.writeAttribute("id", buttonId(context, tab), null);
            w.writeAttribute("class", classes("fr-tabs__tab", icon,
                    isBlank(icon) ? null : "fr-tabs__tab--icon-left"), null);
            w.writeAttribute("tabindex", selected ? "0" : "-1", null);
            w.writeAttribute("role", "tab", null);
            w.writeAttribute("aria-selected", String.valueOf(selected), null);
            w.writeAttribute("aria-controls", tab.getClientId(context), null);
            w.writeAttribute("data-dsfr-faces-item", name, null);
            if (bool(tab, "disabled")) {
                w.writeAttribute("disabled", "disabled", null);
            }
            w.writeAttribute("onclick", onclick(context, panel, name), null);
            facetOrText(context, tab, "header", str(tab, "header"));
            w.endElement("button");
            w.endElement("li");
        }
        w.endElement("ul");

        for (UIComponent child : panel.getChildren()) {
            if (child.isRendered()) {
                child.encodeAll(context);
            }
        }
        w.endElement("div");
    }

    private static String onclick(FacesContext context, DsfrTabPanel panel, String name) {
        String id = panel.getClientId(context);
        return switch (panel.getSwitchType()) {
            case "client" -> "document.getElementById(" + jsString(hiddenId(context, panel)) + ").value=" + jsString(name);
            // the DSFR script must not switch before the ajax response: it would show an empty panel
            case "server" -> "event.stopImmediatePropagation();var f=this.form;if(f){var i=document.createElement('input');"
                    + "i.type='hidden';i.name=" + jsString(switchParam(context, panel)) + ";i.value=" + jsString(name)
                    + ";f.appendChild(i);f.submit();}return false";
            default -> "event.stopImmediatePropagation();" + ajaxRequest(id, id);
        };
    }
}
