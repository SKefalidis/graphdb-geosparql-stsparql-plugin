package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by avataar on 22.04.15..
 */
public class TestDelete extends AbstractGeoSparqlPluginTest {
	@Before
	public void setupConn() throws Exception {
		importData("simple_features_geometries.rdf", RDFFormat.RDFXML);
		importData("geosparql-example.rdf", RDFFormat.RDFXML);

		enablePlugin();
	}

	private List<Value> executeExampleQuery(String example) throws Exception {
		String query = IOUtil.readString(
				TestSpecificationExamples.class.getResourceAsStream("/example" + example + ".sparql"));

        return  executeSparqlQueryWithResult(query, "f");
	}

    @Test
	public void testDeleteGeometry() throws Exception {
		IRI geoUri = SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#DExactGeom");
		connection.remove(geoUri, null, null);
		connection.remove((IRI) null, null, geoUri);

		/* If we delete the geometry DExactGeometry we also lose the match for feature D
		   since DExactGeometry was its default geometry */
		List<Value> result = executeExampleQuery("5");
		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDeleteFeature() throws Exception {
		IRI featUri = SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D");
		connection.remove(featUri, null, null);
		connection.remove((IRI) null, null, featUri);

		/* If we delete the feature D we still have a match for the geometry DExactGeometry */
		List<Value> result = executeExampleQuery("5");
		Assert.assertTrue(result.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#DExactGeom")));
		Assert.assertEquals(1, result.size());
	}
}
