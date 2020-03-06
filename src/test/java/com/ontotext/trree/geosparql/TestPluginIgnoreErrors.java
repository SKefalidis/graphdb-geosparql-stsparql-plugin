package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestPluginIgnoreErrors extends AbstractGeoSparqlPluginTest {

	@Before
	public void setupConn() throws RepositoryException, RDFParseException, IOException {
		importData("geosparql-broken.rdf", RDFFormat.RDFXML);
	}

	@Test (expected = RepositoryException.class)
	public void indexBuildFailsByDefaultOnWrongData() {
		enablePlugin();
	}

	@Test
	public void indexBuildContinuousOnWrongData() {
		executePluginControl(GeoSparqlPlugin.IGNORE_ERRORS_PREDICATE_IRI, VF.createLiteral(true));
		enablePlugin();
	}
}
