package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Some tests with Ordnance Survey data.
 *
 * Tests: GML and British National Grid
 */
@RunWith(Parameterized.class)
public class TestOrdnanceSurveyData extends AbstractGeoSparqlPluginTest {
	@Parameterized.Parameters
	public static Iterable<Object[]> params() {
		return Arrays.asList(new Object[][]{ {false}, {true} });
	}

	private boolean enableFirst;

	public TestOrdnanceSurveyData(boolean enableFirst) {
		this.enableFirst = enableFirst;
	}

	@Before
	public void setupConn() throws Exception {
		File osDir = new File("src/test/resources/ordnancesurvey");
		File[] files = osDir.listFiles();

		if (enableFirst) {
			enablePlugin();
		}

		connection.begin();
		for (File f : files) {
			FileInputStream fis = new FileInputStream(f);
			System.out.println("Adding file " + f);
			connection.add(fis, "urn:base", RDFFormat.TURTLE);
			fis.close();
		}
		connection.commit();

		if (! enableFirst) {
			enablePlugin();
		}
	}

	@Test
	public void testGMLDataAndRCC8Relation() throws Exception {
		System.out.println(new Date());
		String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
				"SELECT *\n" +
				"WHERE { <http://data.ordnancesurvey.co.uk/id/7000000000041323> geo:rcc8tpp ?f }";
		TupleQueryResult tqr = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
		List<String> results = new ArrayList<String>();
		while(tqr.hasNext()) {
			BindingSet bs = tqr.next();
			results.add(bs.getValue("f").stringValue());
			for(String n : bs.getBindingNames()) {
				System.out.println(n + " = " + bs.getValue(n));
			}
		}
		assertTrue(results.contains("http://data.ordnancesurvey.co.uk/id/geometry/41543-6"));
		assertTrue(results.contains("http://data.ordnancesurvey.co.uk/id/7000000000041543"));
	}
}
