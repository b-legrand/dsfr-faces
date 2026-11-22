package fr.gouv.dsfr.faces.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

import java.util.Collection;

/**
 * {@code <dsfr:calendar>}: native date field ({@code input type="date"}). Replaces {@code rich:calendar}. Converts {@code yyyy-MM-dd} to the type of the value ({@code LocalDate}, {@code LocalDateTime}, {@code java.util.Date}, {@code String} formatted with {@code datePattern}).
 * <p>
 * Simple rendering attributes (label, hint, min, max, datePattern, timeZone, disabled, readonly, styleClass) are read from the attribute map by the renderer.
 */
@FacesComponent(DsfrCalendar.COMPONENT_TYPE)
public class DsfrCalendar extends UIInput implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "fr.gouv.dsfr.Calendar";
    public static final String RENDERER_TYPE = "fr.gouv.dsfr.CalendarRenderer";

    public DsfrCalendar() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public Collection<String> getEventNames() {
        return FieldEvents.NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return FieldEvents.DEFAULT;
    }
}
