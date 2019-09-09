package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 29 Sep 2015.
 */
public class TestGeonamesGeoSparqlData extends AbstractGeoSparqlPluginTest {

    private static final IRI[] RESULT_SUBJECTS = {
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/7534683/"),
             SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/resource/Faraglioni"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/3177268/"),
             SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/resource/Monte_Faito"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/3181121/"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/3175082/"),
             SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/resource/Solfatara_(volcano)"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/3172145/"),
             SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/resource/Monte_Nuovo"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/3164481/"),
             SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/resource/Mount_Vesuvius"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/2524582/"),
             SimpleValueFactory.getInstance().createIRI("http://sws.geonames.org/3176910/"),
             SimpleValueFactory.getInstance().createIRI("http://dbpedia.org/resource/Phlegraean_Fields")
    };

    @Before
    public void setupConn() throws Exception {
        importData("geonames_europe_geosparql_data_partial.ttl", RDFFormat.TURTLE);
        enablePlugin();
    }

    @Test
    public void testGeonamesGeosparqlIntersection() throws Exception {
        final List<Value> values = executeSparqlQueryWithResultFromFile("testGeonamesGeoSparqlData", "f");
        assertEquals(14, values.size());
        Set<Value> actual = new HashSet<>(values);
        Set<Value> expected = new HashSet<>(Arrays.asList(RESULT_SUBJECTS));
        assertEquals(expected, actual);
    }
}
