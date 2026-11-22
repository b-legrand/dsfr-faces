package fr.gouv.dsfr.faces.component;

import java.util.Collection;
import java.util.List;

/** Client behavior events (f:ajax, a4j:ajax) of the dsfr-faces form fields. */
final class FieldEvents {

    static final Collection<String> NAMES =
            List.of("valueChange", "change", "blur", "focus", "input", "keydown", "keyup", "click");
    static final String DEFAULT = "valueChange";

    private FieldEvents() {
    }
}
