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
public class TestSpecificationExamples extends AbstractGeoSparqlPluginTest {
	@Parameterized.Parameters
	public static Iterable<Object[]> params() {
		return Arrays.asList(new Object[][]{{false}, {true}});
	}

	private boolean forceRebuild;

	public TestSpecificationExamples(boolean forceRebuild) {
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

	private List<Value> executeExampleQuery(String number) throws Exception {
		return executeSparqlQueryWithResultFromFile("example" + number, "f");
	}

	@Test
	public void testExample1() throws Exception {
		List<Value> result = executeExampleQuery("1");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#B"), result.get(0));
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#F"), result.get(1));
		Assert.assertEquals(2, result.size());
	}

	@Test
	public void testExample1i() throws Exception {
		List<Value> result = executeExampleQuery("1i");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#B"), result.get(0));
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#F"), result.get(1));
		Assert.assertEquals(2, result.size());
	}

	@Test
	public void testExample2() throws Exception {
		List<Value> result = executeExampleQuery("2");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D"), result.get(0));
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testExample2i() throws Exception {
		List<Value> result = executeExampleQuery("2i");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D"), result.get(0));
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testExample3() throws Exception {
		List<Value> result = executeExampleQuery("3");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#C"), result.get(0));
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testExample3i() throws Exception {
		List<Value> result = executeExampleQuery("3i");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#C"), result.get(0));
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void testExample4() throws Exception {
		/*
		 * The original example says the returned entities will be A, D and E but in fact
		 * E is closer than D (reflected in the assertions below).
		 */
		List<Value> result = executeExampleQuery("4");
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#A"), result.get(0));
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#E"), result.get(1));
		Assert.assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D"), result.get(2));
		Assert.assertEquals(3, result.size());
	}

	@Test
	public void testExample5() throws Exception {
		/*
		 * The original example says the results should contain #E and #EExactGeom too,
		 * but that isn't true as the sfOverlaps relation requires geometries of the same
		 * dimension. Geometry A is 2D (polygon), while E is 1D (a line).
		 */
		List<Value> result = executeExampleQuery("5");
        System.out.println(result.size());
        Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D")));
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#DExactGeom")));
		Assert.assertEquals(2, result.size());
	}

	// This is like Example 5 but uses a rewritten query with UNIONs
	@Test
	public void testExample5a() throws Exception {
		List<Value> result = executeExampleQuery("5a");
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D")));
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#DExactGeom")));
		Assert.assertEquals(2, result.size());
	}
}
