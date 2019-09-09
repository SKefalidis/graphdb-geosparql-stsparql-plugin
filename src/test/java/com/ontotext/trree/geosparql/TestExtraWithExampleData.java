package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

/**
 * Uses sample data and sample queries from Annex B of the GeoSPARQL specification.
 */
@RunWith(Parameterized.class)
public class TestExtraWithExampleData extends AbstractGeoSparqlPluginTest {

    @Parameterized.Parameters
	public static Iterable<Object[]> params() {
		return Arrays.asList(new Object[][]{ {false}, {true} });
	}

	private boolean forceRebuild;

	public TestExtraWithExampleData(boolean forceRebuild) {
		this.forceRebuild = forceRebuild;
	}

	@Before
	public void setupConn() throws Exception {
        importData("simple_features_geometries.rdf", RDFFormat.RDFXML);
        importData("geosparql-example.rdf", RDFFormat.RDFXML);

        enablePlugin();

		if (forceRebuild) {
			restartRepositoryAndDeleteIndex();
            enablePlugin();
		}
	}

	// Test query that provides the geometry as a literal in a pattern (custom extension)
	@Test
	public void testLiteral() throws Exception {
		List<Value> result = executeSparqlQueryWithResultFromFile("testLiteral", "f");
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D")));
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#DExactGeom")));
		Assert.assertEquals(2, result.size());
	}

	// Test disjoint no match using index
	@Test
	public void testDisjoint1() throws Exception {
		List<Value> result = executeSparqlQueryWithResultFromFile("testDisjoint1", "f");
		Assert.assertTrue(result.isEmpty());
	}

	// Test disjoint match using index
	@Test
	public void testDisoint2() throws Exception {
		List<Value> result = executeSparqlQueryWithResultFromFile("testDisjoint2", "f");
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#B")));
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D")));
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#E")));
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#F")));
		Assert.assertEquals(4, result.size());
	}
}
