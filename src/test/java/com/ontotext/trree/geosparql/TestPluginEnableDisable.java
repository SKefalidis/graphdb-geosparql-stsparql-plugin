package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 16 Sep 2015.
 */

public class TestPluginEnableDisable extends AbstractGeoSparqlPluginTest {

    @Before
    public void setupConn() throws RepositoryException, RDFParseException, IOException {
        importData("simple_features_geometries.rdf", RDFFormat.RDFXML);
        importData("geosparql-example.rdf", RDFFormat.RDFXML);
    }

    @Test
    public void testPluginDefaultDisabledState() throws Exception {
        final List<Value> values = executeSparqlQueryWithResultFromFile("example5", "f");
        assertTrue(values.isEmpty());
    }

    @Test
    public void testPluginDisabledLiteral() throws Exception {
        enablePlugin();
        disablePlugin();
        final List<Value> values = executeSparqlQueryWithResultFromFile("example5", "f");
        assertTrue(values.isEmpty());
    }

    @Test
    public void testPluginEnabledLiteral() throws Exception {
        enablePlugin();
        final List<Value> values = executeSparqlQueryWithResultFromFile("example5", "f");
        assertEquals(values.size(), 2);
    }

    @Test
    public void testPluginEnabledAfterDisablingLiteral() throws Exception {
        enablePlugin();
        disablePlugin();
        enablePlugin();
        final List<Value> values = executeSparqlQueryWithResultFromFile("example5", "f");
        assertEquals(values.size(), 2);
    }
}
