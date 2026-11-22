package fr.gouv.dsfr.faces.component;

import fr.gouv.dsfr.faces.Dsfr;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code <dsfr:tabPanel>}: DSFR tabs ({@code fr-tabs}) of {@code dsfr:tab} children. Replaces
 * {@code rich:tabPanel}.
 * <p>
 * {@code switchType}, as in RichFaces:
 * <ul>
 *     <li>{@code ajax} (default, as RichFaces): only the active tab is rendered and processed; a tab click
 *     re-renders the panel with ajax, without validating the active tab;</li>
 *     <li>{@code server}: same, with a full request;</li>
 *     <li>{@code client}: every tab is rendered and processed, switching is done by the DSFR script.</li>
 * </ul>
 * The active tab ({@code activeItem}, a tab name) is kept across requests and written back to its value
 * expression if any.
 * <p>
 * Attributes read by the renderer: label (aria-label of the tab list, default « Onglets »), styleClass.
 */
@ResourceDependency(library = "jakarta.faces", name = "faces.js")
@FacesComponent(DsfrTabPanel.COMPONENT_TYPE)
public class DsfrTabPanel extends UIComponentBase {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.TabPanel";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.TabPanelRenderer";

    /** Set by the renderer when the request is a tab switch: the content is then not processed. */
    private transient boolean switching;

    public DsfrTabPanel() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return Dsfr.COMPONENT_FAMILY;
    }

    public String getActiveItem() {
        return (String) getStateHelper().eval("activeItem");
    }

    public void setActiveItem(String activeItem) {
        Expressions.set(this, getStateHelper(), "activeItem", activeItem);
    }

    /** client, ajax (default) or server. */
    public String getSwitchType() {
        Object value = getStateHelper().eval("switchType", "ajax");
        String type = value.toString();
        return switch (type) {
            case "client", "server" -> type;
            default -> "ajax";
        };
    }

    public void setSwitchType(String switchType) {
        getStateHelper().put("switchType", switchType);
    }

    public boolean isClientSwitch() {
        return "client".equals(getSwitchType());
    }

    public void setSwitching(boolean switching) {
        this.switching = switching;
    }

    /** Rendered tabs. */
    public List<DsfrTab> getTabs() {
        List<DsfrTab> tabs = new ArrayList<>();
        for (UIComponent child : getChildren()) {
            if (child instanceof DsfrTab tab && tab.isRendered()) {
                tabs.add(tab);
            }
        }
        return tabs;
    }

    public static String nameOf(UIComponent tab) {
        Object name = tab.getAttributes().get("name");
        return name == null || name.toString().isBlank() ? tab.getId() : name.toString();
    }

    private static boolean isDisabled(UIComponent tab) {
        Object disabled = tab.getAttributes().get("disabled");
        return Boolean.TRUE.equals(disabled) || "true".equals(disabled);
    }

    /** The tab named activeItem, or the first enabled tab. */
    public DsfrTab getActiveTab() {
        List<DsfrTab> tabs = getTabs();
        String active = getActiveItem();
        for (DsfrTab tab : tabs) {
            if (Objects.equals(nameOf(tab), active) && !isDisabled(tab)) {
                return tab;
            }
        }
        return tabs.stream().filter(t -> !isDisabled(t)).findFirst().orElse(null);
    }

    // ------------------------------------------------------------ processing (ajax / server: active tab only)

    private List<UIComponent> processedChildren() {
        if (isClientSwitch()) {
            return getChildren();
        }
        DsfrTab active = getActiveTab();
        List<UIComponent> children = new ArrayList<>();
        for (UIComponent child : getChildren()) {
            if (!(child instanceof DsfrTab) || child == active) {
                children.add(child);
            }
        }
        return children;
    }

    @Override
    public void processDecodes(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        switching = false;
        pushComponentToEL(context, this);
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        } finally {
            popComponentFromEL(context);
        }
        if (switching) {
            return;
        }
        for (UIComponent child : processedChildren()) {
            child.processDecodes(context);
        }
    }

    @Override
    public void processValidators(FacesContext context) {
        if (!isRendered() || switching) {
            return;
        }
        for (UIComponent child : processedChildren()) {
            child.processValidators(context);
        }
    }

    @Override
    public void processUpdates(FacesContext context) {
        if (!isRendered() || switching) {
            return;
        }
        for (UIComponent child : processedChildren()) {
            child.processUpdates(context);
        }
    }
}
