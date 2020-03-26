package com.ontotext.trree.geosparql;

import com.ontotext.trree.entitypool.impl.CustomTripleImpl;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestGeoSparqlPluginWithRDFStarData extends AbstractGeoSparqlPluginTest {
	private static final String EMBEDDED_A_TRIPLE = "<<http://example.org/ApplicationSchema#A http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://example.org/ApplicationSchema#PlaceOfInterest>>";
	private static final String EMBEDDED_B_TRIPLE = "<<http://example.org/ApplicationSchema#B http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://example.org/ApplicationSchema#PlaceOfInterest>>";

	private static final String SEARCH_EMBEDDED_SUBJECT = "PREFIX rank: <http://www.ontotext.com/owlim/RDFRank#>\n" +
			"PREFIX opencyc-en: <http://sw.opencyc.org/2008/06/10/concept/en/>\n" +
			"PREFIX my: <http://example.org/ApplicationSchema#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"SELECT * WHERE {\n" +
			"  ?s my:hasExactGeometry <http://example.org/ApplicationSchema#AExactGeom>.\n" +
			"}";
	private static final String SEARCH_EMBEDDED_OBJECT = "PREFIX my: <http://example.org/ApplicationSchema#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX ns_ext: <http://rdf.useekm.com/ext#>\n" +
			"select * where { \n" +
			"\t<<my:A rdf:type my:PlaceOfInterest>> ns_ext:coveredBy ?o .\n" +
			"}";

	private static final String SEARCH_INDEX_WITH_BOUND_NESTED_TRIPLE = "PREFIX my: <http://example.org/ApplicationSchema#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX ns_ext: <http://rdf.useekm.com/ext#>\n" +
			"select * where { \n" +
			"\t?s ns_ext:coveredBy ?o .\n" +
			"} limit 100 ";

	@Before
	public void setupConn() throws Exception {
		// Insert RDF* data into repository
		importData("geosparql-rdf-star-example.ttls", RDFFormat.TURTLESTAR);
		// Enable GeoSparql plugin
		enablePlugin();
	}

	@Test
	public void shouldProperlyReturnRDFStarDataOnSubjectPosition() throws Exception {
		// Search index
		List<Value> result = executeSparqlQueryWithResult(SEARCH_EMBEDDED_SUBJECT, "s");
		Assert.assertEquals(1, result.size());
		Value returnedValue = result.get(0);
		// Verify that searched value is instance of CustomTripleImpl
		Assert.assertTrue(returnedValue instanceof CustomTripleImpl);
		// Verify that returned result for subject is the embedded triple
		Assert.assertEquals(EMBEDDED_A_TRIPLE, returnedValue.stringValue());
	}

	@Test
	public void shouldProperlyReturnRDFStarDataOnObjectPosition() throws Exception {
		// Search index
		List<Value> result = executeSparqlQueryWithResult(SEARCH_EMBEDDED_OBJECT, "o");
		Assert.assertEquals(1, result.size());
		Value returnedValue = result.get(0);
		// Verify that searched value is instance of CustomTripleImpl
		Assert.assertTrue(returnedValue instanceof CustomTripleImpl);
		// Verify that returned result for object is the embedded triple
		Assert.assertEquals(EMBEDDED_B_TRIPLE, returnedValue.stringValue());
	}

	@Test
	public void shouldBeAbleToSearchIndexWithBoundSubjectNestedTriple() throws Exception {
		List<Value> result = executeSparqlQueryWithResult(
				SEARCH_INDEX_WITH_BOUND_NESTED_TRIPLE.replace("?s", "<<my:A rdf:type my:PlaceOfInterest>>"), "o");
		Assert.assertEquals(1, result.size());
		Value returnedValue = result.get(0);
		// Verify that searched value is instance of CustomTripleImpl
		Assert.assertTrue(returnedValue instanceof CustomTripleImpl);
		// Verify that returned result for object is the embedded triple
		Assert.assertEquals(EMBEDDED_B_TRIPLE, returnedValue.stringValue());
	}

	@Test
	public void shouldBeAbleToSearchIndexWithBoundObjectNestedTriple() throws Exception {
		List<Value> result = executeSparqlQueryWithResult(
				SEARCH_INDEX_WITH_BOUND_NESTED_TRIPLE.replace("?o", "<<my:B rdf:type my:PlaceOfInterest>>"), "s");
		Assert.assertEquals(1, result.size());
		Value returnedValue = result.get(0);
		// Verify that searched value is instance of CustomTripleImpl
		Assert.assertTrue(returnedValue instanceof CustomTripleImpl);
		// Verify that returned result for object is the embedded triple
		Assert.assertEquals(EMBEDDED_A_TRIPLE, returnedValue.stringValue());
	}
}
