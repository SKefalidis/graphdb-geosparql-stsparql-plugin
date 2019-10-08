package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * Tests for status check (SELECT) and command execution (INSERT).
 */
public class TestPluginControl extends AbstractGeoSparqlPluginTest {
    @Test
    public void testEnabled() {
        checkCommand(GeoSparqlPlugin.ENABLED_PREDICATE_IRI, VF.createLiteral(GeoSparqlConfig.ENABLED_DEFAULT));
        boolean newValue = !GeoSparqlConfig.ENABLED_DEFAULT;
        executePluginControl(GeoSparqlPlugin.ENABLED_PREDICATE_IRI, VF.createLiteral(newValue));
        checkCommand(GeoSparqlPlugin.ENABLED_PREDICATE_IRI, VF.createLiteral(newValue));
    }

    @Test
    public void testPrefixTree() {
        // Check default value
        checkCommand(GeoSparqlPlugin.PREFIXTREE_PREDICATE_IRI, GeoSparqlConfig.PREFIXTREE_DEFAULT.toLiteral());
        // Set and check new value
        GeoSparqlConfig.PrefixTree newValue = GeoSparqlConfig.PREFIXTREE_DEFAULT == GeoSparqlConfig.PrefixTree.QUAD ?
                GeoSparqlConfig.PrefixTree.GEOHASH : GeoSparqlConfig.PrefixTree.QUAD;
        executePluginControl(GeoSparqlPlugin.PREFIXTREE_PREDICATE_IRI, newValue.toLiteral());
        checkCommand(GeoSparqlPlugin.PREFIXTREE_PREDICATE_IRI, newValue.toLiteral());
        // Current must remain as before the change
        checkCommand(GeoSparqlPlugin.CURRENT_PREFIXTREE_PREDICATE_IRI, GeoSparqlConfig.PREFIXTREE_DEFAULT.toLiteral());
        // Reindex, current value must become the same as the new value
        enablePlugin();
        forceReindex();
        checkCommand(GeoSparqlPlugin.CURRENT_PREFIXTREE_PREDICATE_IRI, newValue.toLiteral());
    }

    @Test
    public void testPrecision() {
        // Check default value
        checkCommand(GeoSparqlPlugin.PRECISION_PREDICATE_IRI, VF.createLiteral(GeoSparqlConfig.PRECISION_DEFAULT));
        // Set and check new value
        int newValue = GeoSparqlConfig.PRECISION_DEFAULT + 1;
        executePluginControl(GeoSparqlPlugin.PRECISION_PREDICATE_IRI, VF.createLiteral(newValue));
        checkCommand(GeoSparqlPlugin.PRECISION_PREDICATE_IRI, VF.createLiteral(newValue));
        // Current must remain as before the change
        checkCommand(GeoSparqlPlugin.CURRENT_PRECISION_PREDICATE_IRI, VF.createLiteral(GeoSparqlConfig.PRECISION_DEFAULT));
        // Reindex, current value must become the same as the new value
        enablePlugin();
        forceReindex();
        checkCommand(GeoSparqlPlugin.CURRENT_PRECISION_PREDICATE_IRI, VF.createLiteral(newValue));
    }

    @Test
    public void testAllStatus() {
        GraphQuery q = connection.prepareGraphQuery(QueryLanguage.SPARQL,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        q.setBinding("s", GeoSparqlPlugin.CONTEXT_IRI);
        Map<Value, Value> result = new HashMap<>();
        try (GraphQueryResult r = q.evaluate()) {
            while (r.hasNext()) {
                Statement s = r.next();
                assertEquals(GeoSparqlPlugin.CONTEXT_IRI, s.getSubject());
                result.put(s.getPredicate(), s.getObject());
            }
        }
        assertEquals(8, result.size());
        assertEquals(VF.createLiteral(GeoSparqlConfig.ENABLED_DEFAULT), result.get(GeoSparqlPlugin.ENABLED_PREDICATE_IRI));
        assertEquals(GeoSparqlConfig.PREFIXTREE_DEFAULT.toLiteral(), result.get(GeoSparqlPlugin.PREFIXTREE_PREDICATE_IRI));
        assertEquals(VF.createLiteral(GeoSparqlConfig.PRECISION_DEFAULT), result.get(GeoSparqlPlugin.PRECISION_PREDICATE_IRI));
        assertEquals(GeoSparqlConfig.PREFIXTREE_DEFAULT.toLiteral(), result.get(GeoSparqlPlugin.CURRENT_PREFIXTREE_PREDICATE_IRI));
        assertEquals(VF.createLiteral(GeoSparqlConfig.PRECISION_DEFAULT), result.get(GeoSparqlPlugin.CURRENT_PRECISION_PREDICATE_IRI));
        assertEquals(VF.createLiteral(GeoSparqlConfig.MAX_BUFFERED_DOCS_DEFAULT), result.get(GeoSparqlPlugin.MAX_BUFFERED_DOCS_PREDICATE_IRI));
        assertEquals(VF.createLiteral(GeoSparqlConfig.RAM_BUFFER_SIZE_MB_DEFAULT), result.get(GeoSparqlPlugin.RAM_BUFFER_SIZE_MB_PREDICATE_IRI));
        assertEquals(VF.createLiteral(GeoSparqlConfig.IGNORE_ERRORS_DEFAULT), result.get(GeoSparqlPlugin.IGNORE_ERRORS_PREDICATE_IRI));
    }

    @Test
    public void testGetPredicateFromBindSubjectAndObject() {
        TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                String.format("SELECT ?pred { <%s> ?pred \"quad\" }",  GeoSparqlPlugin.CONTEXT_IRI));
        List<Value> boundPredicates = new ArrayList<>();
        try (TupleQueryResult r = q.evaluate()) {
            while (r.hasNext()) {
                boundPredicates.add(r.next().getBinding("pred").getValue());
            }
        }

        // Default config contains two predicates that have value "QUAD"
        assertEquals(2, boundPredicates.size());
        assertTrue(boundPredicates.contains(GeoSparqlPlugin.PREFIXTREE_PREDICATE_IRI));
        assertTrue(boundPredicates.contains(GeoSparqlPlugin.CURRENT_PREFIXTREE_PREDICATE_IRI));
    }

    @Test
    public void testListSubjectAndCertainPredicates() {
        TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                "PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" +
                        "SELECT * WHERE {\n" +
                        "    ?s :currentPrefixTree ?tree;\n" +
                        "       :currentPrecision ?precision;\n" +
                        "        :ramBufferSizeMB ?ramBufferSizeMB;\n" +
                        "        :maxBufferedDocs ?maxBufferedDocs.\n" +
                        "}");
        List<BindingSet> bindingSetList = new ArrayList<>();
        try (TupleQueryResult r = q.evaluate()) {
            while (r.hasNext()) {
                assertTrue(r.hasNext());
                bindingSetList.add(r.next());
            }
        }

        // Verify that single result is returned, containing all queried elements
        assertEquals(1, bindingSetList.size());

        assertEquals(GeoSparqlPlugin.CONTEXT_IRI, bindingSetList.get(0).getBinding("s").getValue());
        assertEquals(GeoSparqlConfig.PREFIXTREE_DEFAULT.toLiteral(), bindingSetList.get(0).getBinding("tree").getValue());
        assertEquals(VF.createLiteral(GeoSparqlConfig.PRECISION_DEFAULT), bindingSetList.get(0).getBinding("precision").getValue());
        assertEquals(VF.createLiteral(GeoSparqlConfig.RAM_BUFFER_SIZE_MB_DEFAULT), bindingSetList.get(0).getBinding("ramBufferSizeMB").getValue());
        assertEquals(VF.createLiteral(GeoSparqlConfig.MAX_BUFFERED_DOCS_DEFAULT), bindingSetList.get(0).getBinding("maxBufferedDocs").getValue());
    }

    @Test
    public void testMaxBufferedDocs() {
        // Check default value
        checkCommand(GeoSparqlPlugin.MAX_BUFFERED_DOCS_PREDICATE_IRI, VF.createLiteral(GeoSparqlConfig.MAX_BUFFERED_DOCS_DEFAULT));
        // Set and check new value
        int newValue = GeoSparqlConfig.MAX_BUFFERED_DOCS_DEFAULT + 1;
        executePluginControl(GeoSparqlPlugin.MAX_BUFFERED_DOCS_PREDICATE_IRI, VF.createLiteral(newValue));
        checkCommand(GeoSparqlPlugin.MAX_BUFFERED_DOCS_PREDICATE_IRI, VF.createLiteral(newValue));
        // Reindex, new value should be stored into config
        enablePlugin();
        forceReindex();
        checkCommand(GeoSparqlPlugin.MAX_BUFFERED_DOCS_PREDICATE_IRI, VF.createLiteral(newValue));
    }

    @Test
    public void testRamBufferSizeMb() {
        // Check default value
        checkCommand(GeoSparqlPlugin.RAM_BUFFER_SIZE_MB_PREDICATE_IRI, VF.createLiteral(GeoSparqlConfig.RAM_BUFFER_SIZE_MB_DEFAULT));
        // Set and check new value
        double newValue = GeoSparqlConfig.RAM_BUFFER_SIZE_MB_DEFAULT + 2.0;
        executePluginControl(GeoSparqlPlugin.RAM_BUFFER_SIZE_MB_PREDICATE_IRI, VF.createLiteral(newValue));
        checkCommand(GeoSparqlPlugin.RAM_BUFFER_SIZE_MB_PREDICATE_IRI, VF.createLiteral(newValue));
        // Reindex, new value should be stored into config
        enablePlugin();
        forceReindex();
        checkCommand(GeoSparqlPlugin.RAM_BUFFER_SIZE_MB_PREDICATE_IRI, VF.createLiteral(newValue));
    }

    private void checkCommand(IRI command, Value expected) {
        TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, "SELECT ?status { ?s ?command ?status }");
        q.setBinding("command", command);
        try (TupleQueryResult r = q.evaluate()) {
            assertTrue(r.hasNext());
            assertEquals(expected, r.next().getBinding("status").getValue());
            assertFalse(r.hasNext());
        }
    }
}
