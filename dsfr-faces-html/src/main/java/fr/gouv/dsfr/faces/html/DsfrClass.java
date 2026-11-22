package fr.gouv.dsfr.faces.html;

import java.util.Map;
import java.util.Set;

/**
 * DSFR class added to the main element of a standard renderer.
 *
 * <p>
 * No error class: outside the search bar, the DSFR styles field errors only through the group
 * ({@code fr-input-group--error}), which needs a wrapper the standard renderers do not produce. Invalid
 * fields get {@code aria-invalid="true"}; the visible error is the adjacent message ({@code h:message},
 * {@code rich:message}). Use {@code dsfr:inputText} for the complete DSFR field.
 *
 * @param elements element names that receive the class (the first one started with the component)
 * @param cssClass DSFR class
 */
record DsfrClass(Set<String> elements, String cssClass) {

    private static final String INPUT = "jakarta.faces.Input";
    private static final String SELECT_ONE = "jakarta.faces.SelectOne";
    private static final String SELECT_MANY = "jakarta.faces.SelectMany";
    private static final String OUTPUT = "jakarta.faces.Output";
    private static final String COMMAND = "jakarta.faces.Command";
    private static final String OUTCOME_TARGET = "jakarta.faces.OutcomeTarget";

    private static final DsfrClass FIELD = new DsfrClass(Set.of("input"), "fr-input");
    private static final DsfrClass TEXTAREA = new DsfrClass(Set.of("textarea"), "fr-input");
    private static final DsfrClass SELECT = new DsfrClass(Set.of("select"), "fr-select");
    private static final DsfrClass LABEL = new DsfrClass(Set.of("label"), "fr-label");
    private static final DsfrClass BUTTON = new DsfrClass(Set.of("input", "button"), "fr-btn");
    private static final DsfrClass LINK = new DsfrClass(Set.of("a"), "fr-link");

    /** Key: component family + '|' + renderer type of the standard HTML_BASIC renderers. */
    static final Map<String, DsfrClass> BY_RENDERER = Map.ofEntries(
            Map.entry(INPUT + "|jakarta.faces.Text", FIELD),           // h:inputText
            Map.entry(INPUT + "|jakarta.faces.Secret", FIELD),         // h:inputSecret
            Map.entry(INPUT + "|jakarta.faces.Textarea", TEXTAREA),    // h:inputTextarea
            Map.entry(SELECT_ONE + "|jakarta.faces.Menu", SELECT),     // h:selectOneMenu
            Map.entry(SELECT_ONE + "|jakarta.faces.Listbox", SELECT),  // h:selectOneListbox
            Map.entry(SELECT_MANY + "|jakarta.faces.Menu", SELECT),    // h:selectManyMenu
            Map.entry(SELECT_MANY + "|jakarta.faces.Listbox", SELECT), // h:selectManyListbox
            Map.entry(OUTPUT + "|jakarta.faces.Label", LABEL),         // h:outputLabel
            Map.entry(COMMAND + "|jakarta.faces.Button", BUTTON),      // h:commandButton
            Map.entry(OUTCOME_TARGET + "|jakarta.faces.Button", BUTTON), // h:button
            Map.entry(COMMAND + "|jakarta.faces.Link", LINK),          // h:commandLink
            Map.entry(OUTPUT + "|jakarta.faces.Link", LINK),           // h:outputLink
            Map.entry(OUTCOME_TARGET + "|jakarta.faces.Link", LINK));  // h:link

    static DsfrClass of(String family, String rendererType) {
        return BY_RENDERER.get(family + "|" + rendererType);
    }
}
