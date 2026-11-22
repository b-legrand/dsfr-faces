package fr.gouv.dsfr.faces.html;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.context.ResponseWriterWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Response writer installed while a standard renderer encodes {@code target}: on the first matching element
 * started with {@code target}, merges the DSFR class into the {@code class} attribute written by the renderer
 * ({@code styleClass}), or adds one when the renderer writes none, just before the start tag is closed.
 * Also adds {@code aria-invalid="true"} to invalid inputs.
 */
final class DsfrClassWriter extends ResponseWriterWrapper {

    private final UIComponent target;
    private final DsfrClass dsfrClass;
    private final boolean invalid;

    private boolean matched;
    private boolean inStartTag;
    private boolean classWritten;
    private boolean ariaInvalidWritten;

    DsfrClassWriter(ResponseWriter wrapped, UIComponent target, DsfrClass dsfrClass, boolean invalid) {
        super(wrapped);
        this.target = target;
        this.dsfrClass = dsfrClass;
        this.invalid = invalid;
    }

    @Override
    public void startElement(String name, UIComponent component) throws IOException {
        closeStartTag();
        super.startElement(name, component);
        if (!matched && component == target && dsfrClass.elements().contains(name)) {
            matched = true;
            inStartTag = true;
        }
    }

    @Override
    public void writeAttribute(String name, Object value, String property) throws IOException {
        if (inStartTag && "class".equals(name)) {
            classWritten = true;
            super.writeAttribute(name, merge(value), property);
            return;
        }
        if (inStartTag && "aria-invalid".equals(name)) {
            ariaInvalidWritten = true;
        }
        super.writeAttribute(name, value, property);
    }

    /** Writes the pending attributes; the underlying writer closes the start tag on the next call. */
    private void closeStartTag() throws IOException {
        if (!inStartTag) {
            return;
        }
        inStartTag = false;
        if (!classWritten) {
            super.writeAttribute("class", merge(null), null);
        }
        if (invalid && !ariaInvalidWritten) {
            super.writeAttribute("aria-invalid", "true", null);
        }
    }

    private String merge(Object existing) {
        Set<String> classes = new LinkedHashSet<>();
        classes.add(dsfrClass.cssClass());
        if (existing != null) {
            classes.addAll(Arrays.asList(existing.toString().trim().split("\\s+")));
        }
        classes.remove("");
        return String.join(" ", classes);
    }

    // ------------------------------------------------------------ every other output closes the start tag

    @Override
    public void endElement(String name) throws IOException {
        closeStartTag();
        super.endElement(name);
    }

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
