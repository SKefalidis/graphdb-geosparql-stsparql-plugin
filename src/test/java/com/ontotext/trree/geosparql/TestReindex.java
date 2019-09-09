package com.ontotext.trree.geosparql;

import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 14 Sep 2015.
 */
public class TestReindex extends AbstractGeoSparqlPluginTest {

    private static final Pattern LUCENE_INDEX_FILES_PATTERN = Pattern.compile(
            "(segments(\\_\\d+|\\.gen)|.*?\\.cfe|.*?\\.cfs|write\\.lock|.*?\\.si|.*?\\.fnm|.*?\\.dim|.*?\\.dvm" +
                    "|.*?\\.fdt|.*?\\.dvd|.*?\\.tip|.*?\\.fdx|.*?\\.dii|.*?\\.doc|.*?\\.tim)$");

    @Before
    public void setupConn() throws Exception {
        importData("simple_features_geometries.rdf", RDFFormat.RDFXML);
        importData("geosparql-example.rdf", RDFFormat.RDFXML);

        enablePlugin();
    }

    @Test(expected = RuntimeException.class)
    public void testFailedQueryAfterDeletedIndex() throws Exception {
        //test with select query
        assertSparqlSelectExample5Results();

        //delete index
        FileUtil.deleteDir(getGeoSparqlStorageDir());
        assertFalse(getGeoSparqlStorageDir().exists());

        // select query should fail
        assertSparqlSelectExample5Results();
    }

    @Test
    public void testReindexThroughSparqlPredicate() throws Exception {
        //test with select query
        assertSparqlSelectExample5Results();

        //delete index
        FileUtil.deleteDir(getGeoSparqlStorageDir());
        assertFalse(getGeoSparqlStorageDir().exists());

        //reindex through SPARQL-Update query
        executeSparqlUpdateQueryFromFile("testForceReindexPredicate");

        //test again with select query
        assertSparqlSelectExample5Results();

        //test if index exists
        final File indexDir = GeoSparqlConfig.resolveIndexPath(getGeoSparqlStorageDir().toPath()).toFile();
        assertTrue(indexDir.listFiles().length > 1);
        assertLuceneIndexFiles(indexDir.listFiles());

    }

    @Test
    public void testNoReindexThroughRepositoryReinit() throws Exception {
        FileUtil.deleteDir(getGeoSparqlStorageDir());
        assertTrue(!getGeoSparqlStorageDir().exists());

        restartRepository();

        final File indexDir = new File(getGeoSparqlStorageDir(), "index");
        assertFalse(indexDir.exists());
    }


    private void assertLuceneIndexFiles(File[] files) {
        for (File file : files) {
            String indexFileName = file.getName();
            Matcher m = LUCENE_INDEX_FILES_PATTERN.matcher(indexFileName);
            assertTrue(file.exists());
            String group = null;
            if (m.matches()) {
                group = m.group(1);
            }
            assertEquals(indexFileName, group);
        }
    }

    private void assertSparqlSelectExample5Results() throws Exception {
        final List<Value> values1 = executeSparqlQueryWithResultFromFile("example5", "f");
        assertEquals(values1.size(), 2);
        assertTrue(values1.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#D")));
        assertTrue(values1.contains(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#DExactGeom")));
    }
}
