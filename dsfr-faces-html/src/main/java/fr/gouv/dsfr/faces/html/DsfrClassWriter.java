package fr.gouv.dsfr.faces.html;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.context.ResponseWriterWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Response writer installed while a standard renderer encodes {@code target}. On the first matching element
 * started with {@code target}:
 * <ul>
 *     <li>merges the DSFR class into the {@code class} attribute written by the renderer
 *     ({@code styleClass}), or adds one just before the start tag is closed;</li>
 *     <li>adds the attributes of the {@link Decoration} ({@code aria-*}), merging {@code aria-describedby};</li>
 *     <li>for a label, writes the required mark and the hint just before its end tag (which may come in a
 *     later encode call: Mojarra and MyFaces start the label in encodeBegin and end it in encodeEnd).</li>
 * </ul>
 */
final class DsfrClassWriter extends ResponseWriterWrapper {

    private static final String LABEL = "label";

    private final UIComponent target;
    private final Decoration decoration;

    private boolean matched;
    private boolean inStartTag;
    private final Set<String> written = new HashSet<>();
    /** Depth of label elements opened through this writer; the target label is at {@link #targetDepth}. */
    private int labelDepth;
    private int targetDepth = -1;
    private boolean suffixWritten;

    DsfrClassWriter(ResponseWriter wrapped, UIComponent target, Decoration decoration) {
        super(wrapped);
        this.target = target;
        this.decoration = decoration;
    }

    @Override
    public void startElement(String name, UIComponent component) throws IOException {
        closeStartTag();
        super.startElement(name, component);
        if (LABEL.equals(name)) {
            labelDepth++;
        }
        if (!matched && component == target && decoration.dsfrClass().elements().contains(name)) {
            matched = true;
            inStartTag = true;
            if (LABEL.equals(name)) {
                targetDepth = labelDepth;
            }
        }
    }

    @Override
    public void writeAttribute(String name, Object value, String property) throws IOException {
        if (inStartTag) {
            written.add(name);
            if ("class".equals(name)) {
                super.writeAttribute(name, mergeClass(value), property);
                return;
            }
            String extra = decoration.attributes().get(name);
            if (extra != null && "aria-describedby".equals(name) && value != null) {
                super.writeAttribute(name, value + " " + extra, property);
                return;
            }
        }
        super.writeAttribute(name, value, property);
    }

    @Override
    public void endElement(String name) throws IOException {
        closeStartTag();
        if (LABEL.equals(name)) {
            // the target label: opened here (matched) or before this encode call (depth 0)
            boolean targetLabel = matched ? labelDepth == targetDepth : labelDepth == 0;
            if (targetLabel && !suffixWritten && decoration.hasLabelSuffix()) {
                writeLabelSuffix();
            }
            if (labelDepth > 0) {
                labelDepth--;
            }
        }
        super.endElement(name);
    }

    /** {@code " *<span class="fr-hint-text">hint</span>"}, as dsfr:inputText. */
    private void writeLabelSuffix() throws IOException {
        suffixWritten = true;
        if (decoration.required()) {
            super.writeText(" *", null);
        }
        String hint = decoration.hint();
        if (hint != null && !hint.isBlank()) {
            super.startElement("span", null);
            super.writeAttribute("class", "fr-hint-text", null);
            super.writeText(hint, null);
            super.endElement("span");
        }
    }

    /** Writes the pending attributes; the underlying writer closes the start tag on the next call. */
    private void closeStartTag() throws IOException {
        if (!inStartTag) {
            return;
        }
        inStartTag = false;
        if (!written.contains("class")) {
            super.writeAttribute("class", mergeClass(null), null);
        }
        for (Map.Entry<String, String> entry : decoration.attributes().entrySet()) {
            if (!written.contains(entry.getKey())) {
                super.writeAttribute(entry.getKey(), entry.getValue(), null);
            }
        }
    }

    private String mergeClass(Object existing) {
        Set<String> classes = new LinkedHashSet<>();
        classes.add(decoration.dsfrClass().cssClass());
        if (existing != null) {
            classes.addAll(Arrays.asList(existing.toString().trim().split("\\s+")));
        }
        classes.remove("");
        return String.join(" ", classes);
    }

    // ------------------------------------------------------------ every other output closes the start tag

    @Override
    public void writeText(Object text, String property) throws IOException {
        closeStartTag();
        super.writeText(text, property);
    }

    @Override
    public void writeText(Object text, UIComponent component, String property) throws IOException {
        closeStartTag();
        super.writeText(text, component, property);
    }

    @Override
    public void writeText(char[] text, int off, int len) throws IOException {
        closeStartTag();
        super.writeText(text, off, len);
    }

    @Override
    public void writeComment(Object comment) throws IOException {
        closeStartTag();
        super.writeComment(comment);
    }

    @Override
    public void startCDATA() throws IOException {
        closeStartTag();
        super.startCDATA();
    }

    @Override
    public void endCDATA() throws IOException {
        closeStartTag();
        super.endCDATA();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        closeStartTag();
        super.write(cbuf, off, len);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        closeStartTag();
        super.write(cbuf);
    }

    @Override
    public void write(int c) throws IOException {
        closeStartTag();
        super.write(c);
    }

    @Override
    public void write(String str) throws IOException {
        closeStartTag();
        super.write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        closeStartTag();
        super.write(str, off, len);
    }

    @Override
    public void flush() throws IOException {
        closeStartTag();
        super.flush();
    }

    @Override
    public void close() throws IOException {
        closeStartTag();
        super.close();
    }

    /** Called by the renderer wrapper when the renderer returns, in case nothing followed the start tag. */
    void finish() throws IOException {
        closeStartTag();
    }
}
