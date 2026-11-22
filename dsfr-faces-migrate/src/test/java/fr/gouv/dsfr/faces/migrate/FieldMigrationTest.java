package fr.gouv.dsfr.faces.migrate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldMigrationTest {

    private static final String HEAD = """
            <html xmlns:h="http://xmlns.jcp.org/jsf/html"
                  xmlns:dsfr="http://richfaces.org/rich">
            """;

    @Test
    void wrapsLabelFieldAndMessageAfterSed() throws Exception {
        String source = HEAD + """
                <h:form id="f">
                    <h:outputLabel for="name" value="Nom"/>
                    <h:inputText id="name" value="#{b.name}" required="true"/>
                    <dsfr:message id="nameMsg" for="name"/>
                </h:form>
                </html>
                """;
        String expected = HEAD + """
                <h:form id="f">
                    <dsfr:field id="nameField">
                        <h:outputLabel for="name" value="Nom"/>
                        <h:inputText id="name" value="#{b.name}" required="true"/>
                        <dsfr:message id="nameMsg" for="name"/>
                    </dsfr:field>
                </h:form>
                </html>
                """;
        FieldMigration.Result result = FieldMigration.migrate(source);
        assertEquals(expected, result.content());
        assertEquals(1, result.wrapped());
        assertTrue(result.notes().isEmpty());
    }

    @Test
    void wrapsSelectWithoutMessageAndKeepsChildren() throws Exception {
        String source = HEAD + """
                  <h:outputLabel for="topic" value="Sujet"/>
                  <h:selectOneMenu id="topic" value="#{b.topic}">
                    <f:selectItem itemLabel="A" itemValue="a"/>
                  </h:selectOneMenu>
                </html>
                """;
        String expected = HEAD + """
                  <dsfr:field id="topicField">
                    <h:outputLabel for="topic" value="Sujet"/>
                    <h:selectOneMenu id="topic" value="#{b.topic}">
                      <f:selectItem itemLabel="A" itemValue="a"/>
                    </h:selectOneMenu>
                  </dsfr:field>
                </html>
                """;
        assertEquals(expected, FieldMigration.migrate(source).content());
    }

    @Test
    void richfacesPrefixBeforeSedAndHtmlMessage() throws Exception {
        String source = """
                <html xmlns:h="http://java.sun.com/jsf/html" xmlns:rich="http://richfaces.org/rich">
                <h:outputLabel for="a" value="A"/><h:inputText id="a"/><h:message for="a"/>
                </html>
                """;
        String expected = """
                <html xmlns:h="http://java.sun.com/jsf/html" xmlns:rich="http://richfaces.org/rich">
                <rich:field id="aField">
                    <h:outputLabel for="a" value="A"/><h:inputText id="a"/><h:message for="a"/>
                </rich:field>
                </html>
                """;
        assertEquals(expected, FieldMigration.migrate(source).content());
    }

    @Test
    void declaresDsfrNamespaceWhenMissing() throws Exception {
        String source = """
                <html xmlns="http://www.w3.org/1999/xhtml"
                      xmlns:h="jakarta.faces.html">
                <div>
                  <h:outputLabel for="a" value="A"/>
                  <h:inputSecret id="a"/>
                </div>
                </html>
                """;
        String expected = """
                <html xmlns="http://www.w3.org/1999/xhtml"
                      xmlns:h="jakarta.faces.html"
                      xmlns:dsfr="http://gouv.fr/dsfr-faces">
                <div>
                  <dsfr:field id="aField">
                    <h:outputLabel for="a" value="A"/>
                    <h:inputSecret id="a"/>
                  </dsfr:field>
                </div>
                </html>
                """;
        assertEquals(expected, FieldMigration.migrate(source).content());
    }

    @Test
    void isIdempotent() throws Exception {
        String source = HEAD + """
                <h:outputLabel for="a" value="A"/>
                <h:inputText id="a"/>
                </html>
                """;
        String once = FieldMigration.migrate(source).content();
        FieldMigration.Result twice = FieldMigration.migrate(once);
        assertEquals(once, twice.content());
        assertEquals(0, twice.wrapped());
        assertTrue(twice.notes().isEmpty());
    }

    @Test
    void leavesPanelGridAndNonAdjacentLabelsAndReportsThem() throws Exception {
        String source = HEAD + """
                <h:panelGrid columns="2">
                  <h:outputLabel for="a" value="A"/>
                  <h:inputText id="a"/>
                </h:panelGrid>
                <h:outputLabel for="b" value="B"/>
                <div><h:inputText id="b"/></div>
                <h:inputTextarea value="#{x}"/>
                </html>
                """;
        FieldMigration.Result result = FieldMigration.migrate(source);
        assertEquals(source, result.content());
        assertEquals(3, result.notes().size());
        assertEquals(5, result.notes().get(0).line());
        assertTrue(result.notes().get(0).message().contains("h:panelGrid"));
        assertTrue(result.notes().get(1).message().contains("pas de h:outputLabel for=\"b\""));
        assertTrue(result.notes().get(2).message().contains("pas d'id"));
    }

    @Test
    void neverReindentsInsideAttributeValuesAndAllowsCommentsBetween() throws Exception {
        String source = HEAD + """
                <h:outputLabel for="a" value="A"/>
                <!-- commentaire
                sur deux lignes -->
                <h:inputText id="a" onblur="un();
                deux();"/>
                </html>
                """;
        String expected = HEAD + """
                <dsfr:field id="aField">
                    <h:outputLabel for="a" value="A"/>
                    <!-- commentaire
                sur deux lignes -->
                    <h:inputText id="a" onblur="un();
                deux();"/>
                </dsfr:field>
                </html>
                """;
        assertEquals(expected, FieldMigration.migrate(source).content());
    }

    @Test
    void textBetweenLabelAndFieldPreventsWrapping() throws Exception {
        String source = HEAD + """
                <h:outputLabel for="a" value="A"/> : <h:inputText id="a"/>
                </html>
                """;
        FieldMigration.Result result = FieldMigration.migrate(source);
        assertEquals(source, result.content());
        assertEquals(1, result.notes().size());
    }

    @Test
    void generatedIdsAreUnique() throws Exception {
        String source = HEAD + """
                <div id="aField"/>
                <h:outputLabel for="a" value="A"/>
                <h:inputText id="a"/>
                </html>
                """;
        assertTrue(FieldMigration.migrate(source).content().contains("<dsfr:field id=\"aField2\">"));
    }

    @Test
    void reportsMalformedXmlWithLine() {
        XmlLite.ParseException e = assertThrows(XmlLite.ParseException.class,
                () -> FieldMigration.migrate("<a>\n<b>\n</a>"));
        assertTrue(e.getMessage().startsWith("line 3"), e.getMessage());
    }
}
