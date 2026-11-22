package fr.gouv.dsfr.faces.migrate;

import fr.gouv.dsfr.faces.migrate.XmlLite.Document;
import fr.gouv.dsfr.faces.migrate.XmlLite.Element;
import fr.gouv.dsfr.faces.migrate.XmlLite.Node;
import fr.gouv.dsfr.faces.migrate.XmlLite.Other;
import fr.gouv.dsfr.faces.migrate.XmlLite.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optional migration (after the mandatory sed): wraps each
 * {@code h:outputLabel for="x"} + field {@code id="x"} [+ {@code h:message} / {@code rich:message for="x"}]
 * sequence of sibling elements in {@code <dsfr:field id="xField">}, so that the page gets the DSFR field
 * group without changing any component id.
 * <ul>
 *     <li>Only whitespace and comments may separate the elements.</li>
 *     <li>Not inside {@code h:panelGrid} (each child is a cell) nor inside an existing field.</li>
 *     <li>The text of the file is kept: only the wrapper is inserted and the wrapped lines re-indented
 *     (never inside attribute values, comments or CDATA).</li>
 *     <li>Idempotent: running it twice changes nothing.</li>
 *     <li>Fields that are not wrapped are reported.</li>
 * </ul>
 * The {@code field} tag is resolved through the prefix bound to {@code http://gouv.fr/dsfr-faces}, or to
 * {@code http://richfaces.org/rich} (the compatibility taglib declares it too: after the sed this prefix is
 * {@code dsfr}); otherwise {@code xmlns:dsfr="http://gouv.fr/dsfr-faces"} is declared on the root element.
 */
public final class FieldMigration {

    static final String DSFR_URI = "http://gouv.fr/dsfr-faces";
    static final String RICH_URI = "http://richfaces.org/rich";
    private static final Set<String> HTML_URIS = Set.of(
            "jakarta.faces.html", "http://xmlns.jcp.org/jsf/html", "http://java.sun.com/jsf/html");
    private static final Set<String> FIELDS = Set.of(
            "inputText", "inputSecret", "inputTextarea",
            "selectOneMenu", "selectOneListbox", "selectManyMenu", "selectManyListbox");
    private static final String DEFAULT_INDENT = "    ";

    /** Migration result of one file. */
    public record Result(String content, int wrapped, List<Note> notes) {
        public boolean changed(String original) {
            return !content.equals(original);
        }
    }

    /** A field left as is, to review by hand. */
    public record Note(int line, String message) {
    }

    private record Edit(int start, int end, String replacement) {
    }

    private FieldMigration() {
    }

    public static Result migrate(String source) throws XmlLite.ParseException {
        Document doc = XmlLite.parse(source);
        List<Edit> edits = new ArrayList<>();
        List<Note> notes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Element e : doc.allElements()) {
            String id = e.attributes.get("id");
            if (id != null) {
                ids.add(id);
            }
        }

        String prefix = doc.root.prefixOf(DSFR_URI);
        if (prefix == null) {
            prefix = doc.root.prefixOf(RICH_URI);
        }
        String declaration = null;
        if (prefix == null) {
            prefix = doc.root.namespaceOf("dsfr") == null ? "dsfr" : "dsfrf";
            declaration = "xmlns:" + prefix + "=\"" + DSFR_URI + "\"";
        }

        Map<Element, Boolean> wrapped = new IdentityHashMap<>();
        for (Element parent : doc.allElements()) {
            if (isHtml(parent, "panelGrid") || isField(parent)) {
                continue;
            }
            List<Node> items = significant(parent.children);
            for (int i = 0; i < items.size(); i++) {
                if (!(items.get(i) instanceof Element label) || !isHtml(label, "outputLabel")) {
                    continue;
                }
                String forId = label.attributes.get("for");
                if (forId == null || forId.contains("#{") || forId.contains("${")) {
                    continue;
                }
                if (i + 1 >= items.size() || !(items.get(i + 1) instanceof Element field) || !isField(field, forId)) {
                    continue;
                }
                Element last = field;
                if (i + 2 < items.size() && items.get(i + 2) instanceof Element message && isMessage(message, forId)) {
                    last = message;
                }
                String fieldId = uniqueId(forId + "Field", ids);
                edits.add(wrap(doc, parent, label, last, prefix, fieldId));
                wrapped.put(field, Boolean.TRUE);
                i += last == field ? 1 : 2;
            }
        }

        for (Element e : doc.allElements()) {
            if (isHtml(e) && FIELDS.contains(e.localName()) && !wrapped.containsKey(e) && !isField(e.parent)) {
                notes.add(new Note(doc.line(e.start), reason(e)));
            }
        }

        if (declaration != null && !edits.isEmpty()) {
            edits.add(declare(doc, declaration));
        }
        edits.sort(Comparator.comparingInt(Edit::start).reversed());
        StringBuilder out = new StringBuilder(source);
        for (Edit edit : edits) {
            out.replace(edit.start(), edit.end(), edit.replacement());
        }
        return new Result(out.toString(), wrapped.size(), notes);
    }

    private static String reason(Element field) {
        String id = field.attributes.get("id");
        String tag = "<" + field.name + (id == null ? "" : " id=\"" + id + "\"") + ">";
        if (id == null) {
            return tag + " : pas d'id, non enveloppé";
        }
        if (field.parent != null && isHtml(field.parent, "panelGrid")) {
            return tag + " : dans un h:panelGrid (chaque enfant est une cellule), non enveloppé";
        }
        return tag + " : pas de h:outputLabel for=\"" + id + "\" juste avant, non enveloppé";
    }

    /** Elements and significant text (non blank text, CDATA); comments and blank text are skipped. */
    private static List<Node> significant(List<Node> children) {
        List<Node> list = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof Element
                    || (child instanceof Text t && !t.blank())
                    || (child instanceof Other o && o.significant())) {
                list.add(child);
            }
        }
        return list;
    }

    private static Edit wrap(Document doc, Element parent, Element first, Element last, String prefix, String id) {
        String src = doc.source;
        int lineStart = src.lastIndexOf('\n', first.start - 1) + 1;
        String before = src.substring(lineStart, first.start);
        boolean ownLine = before.isBlank();
        String open = "<" + prefix + ":field id=\"" + id + "\">";
        String close = "</" + prefix + ":field>";
        String content = src.substring(first.start, last.end);
        if (!ownLine) {
            return new Edit(first.start, last.end, open + content + close);
        }
        String unit = indentUnit(src, parent, before);
        StringBuilder inner = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            inner.append(c);
            if (c == '\n' && !doc.isProtected(first.start + i)) {
                inner.append(unit);
            }
        }
        return new Edit(first.start, last.end,
                open + "\n" + before + unit + inner + "\n" + before + close);
    }

    /** Indentation step of the file around the parent: child indent minus parent indent, 4 spaces by default. */
    private static String indentUnit(String src, Element parent, String childIndent) {
        int lineStart = src.lastIndexOf('\n', parent.start - 1) + 1;
        String parentIndent = src.substring(lineStart, parent.start);
        if (parentIndent.isBlank() && childIndent.startsWith(parentIndent) && childIndent.length() > parentIndent.length()) {
            return childIndent.substring(parentIndent.length());
        }
        return childIndent.contains("\t") ? "\t" : DEFAULT_INDENT;
    }

    /** Adds the namespace declaration at the end of the root start tag, aligned with its attributes. */
    private static Edit declare(Document doc, String declaration) {
        Element root = doc.root;
        int close = root.startTagEnd - 1; // '>'
        if (doc.source.charAt(close - 1) == '/') {
            close--;
        }
        String tag = doc.source.substring(root.start, close);
        int lastBreak = tag.lastIndexOf('\n');
        String insert;
        if (lastBreak < 0) {
            insert = " " + declaration;
        } else {
            String lastLine = tag.substring(lastBreak + 1);
            String indent = lastLine.substring(0, lastLine.length() - lastLine.stripLeading().length());
            insert = "\n" + indent + declaration;
        }
        return new Edit(close, close, insert);
    }

    private static String uniqueId(String base, Set<String> ids) {
        String id = base;
        for (int i = 2; ids.contains(id); i++) {
            id = base + i;
        }
        ids.add(id);
        return id;
    }

    private static boolean isHtml(Element e) {
        String uri = e.namespace();
        return uri != null && HTML_URIS.contains(uri);
    }

    private static boolean isHtml(Element e, String localName) {
        return isHtml(e) && e.localName().equals(localName);
    }

    private static boolean isField(Element e, String id) {
        return isHtml(e) && FIELDS.contains(e.localName()) && id.equals(e.attributes.get("id"));
    }

    private static boolean isMessage(Element e, String forId) {
        String uri = e.namespace();
        boolean message = e.localName().equals("message")
                && (isHtml(e) || RICH_URI.equals(uri) || DSFR_URI.equals(uri));
        return message && forId.equals(e.attributes.get("for"));
    }

    /** dsfr:field (either URI). */
    private static boolean isField(Element e) {
        if (e == null || !e.localName().equals("field")) {
            return false;
        }
        String uri = e.namespace();
        return DSFR_URI.equals(uri) || RICH_URI.equals(uri);
    }
}
