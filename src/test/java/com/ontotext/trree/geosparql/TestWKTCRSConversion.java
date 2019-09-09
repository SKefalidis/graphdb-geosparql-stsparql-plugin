package com.ontotext.trree.geosparql;

import com.useekm.types.GeoConvert;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestWKTCRSConversion extends AbstractGeoSparqlPluginTest {
	private final static String EXPECTED_SUBJECT_RESULT = "http://data.bigdatagrapes.eu/resource/AUA/estate/Fasoulis/Geotrisi/geo";
	private final static String EXPECTED_WITHIN_RESULT = "http://example.org/ApplicationSchema#F";
	private final static String SUBJECT_SEARCH_QUERY_WITH_DEFAULT_CRS = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
			"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
			"PREFIX onto: <http://www.ontotext.com/>\n" +
			"\n" +
			"SELECT ?s\n" +
			"WHERE {\n" +
			"    ?s geo:sfWithin \"\"\"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>\n" +
			// Note that imported data is in another CRS type "EPSG:32634" and here we're searching
			// on the converted "CRS:84" type. Same will be result if the search is on "EPSG:32634"
			"     %s  \n" +
			"  \"\"\"^^geo:wktLiteral.\n" +
			"}";
	private final static String SUBJECT_SEARCH_QUERY_WITH_EPSG_32634_CRS = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
			"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
			"PREFIX onto: <http://www.ontotext.com/>\n" +
			"\n" +
			"SELECT ?s\n" +
			"WHERE {\n" +
			"    ?s geo:sfWithin \"\"\"<http://www.opengis.net/def/crs/EPSG/0/32634>\n" +
			// Note that imported data is in another CRS type "EPSG:32634" here search is on
			// on imported it.
			"     POINT(799997.80 4589779.63)  \n" +
			"  \"\"\"^^geo:wktLiteral.\n" +
			"}";

	private final static String SEARCH_POINT_IN_POLYGON_QUERY = "PREFIX my: <http://example.org/ApplicationSchema#>\n" +
			"PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
			"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
			"\n" +
			"SELECT ?f\n" +
			"WHERE {\n" +
			"    my:A my:hasExactGeometry ?aGeom .\n" +
			"    ?aGeom geo:asWKT ?aWKT .\n" +
			"    ?f my:hasExactGeometry ?fGeom .\n" +
			"    ?fGeom geo:asWKT ?fWKT .\n" +
			"    FILTER (geof:sfContains(?aWKT, ?fWKT) && !sameTerm(?aGeom, ?fGeom))\n" +
			"}";
	@Test
	public void shouldProperlyConvertAndIndexDifferentCRSThanDefault() throws Exception {
		final String pointInCRS84 = GeoConvert
				.wktToGeometry("<http://www.opengis.net/def/crs/EPSG/0/32634> POINT(799997.80 4589779.63)")
				.toString();

		importData("gdb3142.ttl", RDFFormat.TURTLE);
		enablePlugin();
		List<Value> resultValues = executeSparqlQueryWithResult(String.format(SUBJECT_SEARCH_QUERY_WITH_DEFAULT_CRS, pointInCRS84), "s");

		Assert.assertTrue("Should return one result", !resultValues.isEmpty());
		Assert.assertTrue("Should return result matching subject", EXPECTED_SUBJECT_RESULT.equals(resultValues.get(0).stringValue()));

		resultValues = executeSparqlQueryWithResult(SUBJECT_SEARCH_QUERY_WITH_EPSG_32634_CRS, "s");
		Assert.assertTrue("Should return one result", !resultValues.isEmpty());
		Assert.assertTrue("Should return result matching subject", EXPECTED_SUBJECT_RESULT.equals(resultValues.get(0).stringValue()));
	}

	@Test
	public void shouldProperlyConvertDifferentCRSThanDefaultAndSearchBy() throws Exception {
		// Data in this file is in "EPSG:32634" CRS type
		importData("modified_geosparql-example.rdf", RDFFormat.RDFXML);
		enablePlugin();
		List<Value> resultValues = executeSparqlQueryWithResult(SEARCH_POINT_IN_POLYGON_QUERY, "f");

		Assert.assertTrue("Should return one result", !resultValues.isEmpty());
		Assert.assertTrue("Should return result matching F my favorite place", EXPECTED_WITHIN_RESULT.equals(resultValues.get(0).stringValue()));
	}
}
