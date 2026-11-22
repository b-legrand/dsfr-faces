package fr.gouv.dsfr.faces.migrate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal XML parser for Facelets pages that keeps the offset of every element in the source, so that
 * the migration can edit the text in place and keep the formatting of the file (a DOM or XSLT round trip
 * would rewrite it). Also records the ranges where a line break must not be re-indented (attribute values,
 * comments, CDATA sections, processing instructions, doctype).
 */
final class XmlLite {

    sealed interface Node permits Element, Text, Other {
    }

    /** Text between markup. */
    record Text(int start, int end, boolean blank) implements Node {
    }

    /** Comment, CDATA, processing instruction or doctype. CDATA is significant content. */
    record Other(int start, int end, boolean significant) implements Node {
    }

    static final class Element implements Node {
        final String name;
        final Map<String, String> attributes = new LinkedHashMap<>();
        final List<Node> children = new ArrayList<>();
        final Element parent;
        final int start;
        int startTagEnd;
        int end;

        Element(String name, Element parent, int start) {
            this.name = name;
            this.parent = parent;
            this.start = start;
        }

        String prefix() {
            int colon = name.indexOf(':');
            return colon < 0 ? "" : name.substring(0, colon);
        }

        String localName() {
            int colon = name.indexOf(':');
            return colon < 0 ? name : name.substring(colon + 1);
        }

        /** Namespace URI of a prefix in scope ("" for the default namespace), or null. */
        String namespaceOf(String prefix) {
            String attribute = prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix;
            for (Element e = this; e != null; e = e.parent) {
                String uri = e.attributes.get(attribute);
                if (uri != null) {
                    return uri;
                }
            }
            return null;
        }

        String namespace() {
            return namespaceOf(prefix());
        }

        /** A prefix in scope bound to this URI, or null. */
        String prefixOf(String uri) {
            for (Element e = this; e != null; e = e.parent) {
                for (Map.Entry<String, String> a : e.attributes.entrySet()) {
                    if (a.getKey().startsWith("xmlns:") && a.getValue().equals(uri)) {
                        String prefix = a.getKey().substring(6);
                        if (uri.equals(namespaceOf(prefix))) { // not redeclared below
                            return prefix;
                        }
                    }
                }
            }
            return null;
        }

        List<Element> childElements() {
            List<Element> list = new ArrayList<>();
            for (Node child : children) {
                if (child instanceof Element e) {
                    list.add(e);
                }
            }
            return list;
        }
    }

    static final class Document {
        final String source;
        final Element root;
        final List<int[]> protectedRanges;

        Document(String source, Element root, List<int[]> protectedRanges) {
            this.source = source;
            this.root = root;
            this.protectedRanges = protectedRanges;
        }

        boolean isProtected(int offset) {
            for (int[] range : protectedRanges) {
                if (offset >= range[0] && offset < range[1]) {
                    return true;
                }
            }
            return false;
        }

        int line(int offset) {
            int line = 1;
            for (int i = 0; i < offset && i < source.length(); i++) {
                if (source.charAt(i) == '\n') {
                    line++;
                }
            }
            return line;
        }

        List<Element> allElements() {
            List<Element> list = new ArrayList<>();
            Deque<Element> todo = new ArrayDeque<>();
            todo.push(root);
            while (!todo.isEmpty()) {
                Element e = todo.pop();
                list.add(e);
                List<Element> children = e.childElements();
                for (int i = children.size() - 1; i >= 0; i--) {
                    todo.push(children.get(i));
                }
            }
            return list;
        }
    }

    static final class ParseException extends Exception {
        ParseException(String message) {
            super(message);
        }
    }

    private final String s;
    private final List<int[]> protectedRanges = new ArrayList<>();
    private int pos;

    private XmlLite(String source) {
        this.s = source;
    }

    static Document parse(String source) throws ParseException {
        return new XmlLite(source).parse();
    }

    private Document parse() throws ParseException {
        Deque<Element> stack = new ArrayDeque<>();
        Element root = null;
        int n = s.length();
        while (pos < n) {
            Element parent = stack.peek();
            if (s.startsWith("<!--", pos)) {
                int end = indexAfter("-->", pos + 4);
                add(parent, new Other(pos, end, false));
                protectedRanges.add(new int[]{pos, end});
                pos = end;
            } else if (s.startsWith("<![CDATA[", pos)) {
                int end = indexAfter("]]>", pos + 9);
                add(parent, new Other(pos, end, true));
                protectedRanges.add(new int[]{pos, end});
                pos = end;
            } else if (s.startsWith("<?", pos)) {
                int end = indexAfter("?>", pos + 2);
                add(parent, new Other(pos, end, false));
                protectedRanges.add(new int[]{pos, end});
                pos = end;
            } else if (s.startsWith("<!", pos)) {
                int end = indexAfter(">", pos + 2);
                add(parent, new Other(pos, end, false));
                protectedRanges.add(new int[]{pos, end});
                pos = end;
            } else if (s.startsWith("</", pos)) {
                int end = indexAfter(">", pos + 2);
                String name = s.substring(pos + 2, end - 1).trim();
                Element open = stack.poll();
                if (open == null || !open.name.equals(name)) {
                    throw error("unexpected </" + name + ">" + (open == null ? "" : ", expected </" + open.name + ">"));
                }
                open.end = end;
                pos = end;
            } else if (s.charAt(pos) == '<') {
                Element element = startTag(parent);
                if (parent == null) {
                    if (root != null) {
                        throw error("more than one root element");
                    }
                    root = element;
                } else {
                    parent.children.add(element);
                }
                if (element.end == 0) {
                    stack.push(element);
                }
            } else {
                int next = s.indexOf('<', pos);
                int end = next < 0 ? n : next;
                add(parent, new Text(pos, end, s.substring(pos, end).isBlank()));
                pos = end;
            }
        }
        if (!stack.isEmpty()) {
            throw error("<" + stack.peek().name + "> is not closed");
        }
        if (root == null) {
            throw error("no root element");
        }
        return new Document(s, root, protectedRanges);
    }

    private Element startTag(Element parent) throws ParseException {
        int start = pos;
        pos++;
        int nameStart = pos;
        while (pos < s.length() && !Character.isWhitespace(s.charAt(pos)) && s.charAt(pos) != '>'
                && s.charAt(pos) != '/') {
            pos++;
        }
        Element element = new Element(s.substring(nameStart, pos), parent, start);
        while (true) {
            skipWhitespace();
            if (pos >= s.length()) {
                throw error("unterminated start tag <" + element.name);
            }
            if (s.startsWith("/>", pos)) {
                pos += 2;
                element.startTagEnd = pos;
                element.end = pos;
                return element;
            }
            if (s.charAt(pos) == '>') {
                pos++;
                element.startTagEnd = pos;
                return element;
            }
            int attrStart = pos;
            while (pos < s.length() && s.charAt(pos) != '=' && !Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
            String attrName = s.substring(attrStart, pos);
            skipWhitespace();
            if (pos >= s.length() || s.charAt(pos) != '=') {
                throw error("attribute " + attrName + " without value in <" + element.name + ">");
            }
            pos++;
            skipWhitespace();
            char quote = pos < s.length() ? s.charAt(pos) : 0;
            if (quote != '"' && quote != '\'') {
                throw error("unquoted value for " + attrName + " in <" + element.name + ">");
            }
            int valueStart = pos + 1;
            int valueEnd = s.indexOf(quote, valueStart);
            if (valueEnd < 0) {
                throw error("unterminated value for " + attrName);
            }
            element.attributes.put(attrName, s.substring(valueStart, valueEnd));
            protectedRanges.add(new int[]{valueStart, valueEnd});
            pos = valueEnd + 1;
        }
    }

    private void add(Element parent, Node node) {
        if (parent != null) {
            parent.children.add(node);
        }
    }

    private void skipWhitespace() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
    }

    private int indexAfter(String token, int from) throws ParseException {
        int i = s.indexOf(token, from);
        if (i < 0) {
            throw error("missing " + token);
        }
        return i + token.length();
    }

    private ParseException error(String message) {
        int line = 1;
        for (int i = 0; i < pos && i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                line++;
            }
        }
        return new ParseException("line " + line + ": " + message);
    }
}
