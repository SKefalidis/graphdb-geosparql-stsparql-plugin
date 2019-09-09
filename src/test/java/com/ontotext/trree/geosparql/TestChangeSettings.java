package com.ontotext.trree.geosparql;

import com.ontotext.trree.OwlimSchemaRepository;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.QuadPrefixTree;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * Verifies that the prefix tree and the precision can be changed and that the changes affect the index on the next
 * full indexing operation.
 */
public class TestChangeSettings extends AbstractGeoSparqlPluginTest {
    @Before
    public void setupConn() throws Exception {
        importData("simple_features_geometries.rdf", RDFFormat.RDFXML);
        importData("geosparql-example.rdf", RDFFormat.RDFXML);
    }

    private List<Value> executeExampleQuery(String number) throws Exception {
        return executeSparqlQueryWithResultFromFile("example" + number, "f");
    }

    private void assertQuery() throws Exception {
        List<Value> result = executeExampleQuery("1i");
        assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#B"), result.get(0));
        assertEquals(SimpleValueFactory.getInstance().createIRI("http://example.org/ApplicationSchema#F"), result.get(1));
        assertEquals(2, result.size());
    }

    private String getSetting(String settingName) {
        try (RepositoryConnection connection = repository.getConnection()) {
            TupleQuery tq = connection.prepareTupleQuery(
                    String.format("PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" +
                            "\n" +
                            "SELECT ?setting WHERE {\n" +
                            "    _:s :%s ?setting;\n" +
                            "}", settingName));
            try (TupleQueryResult tqr = tq.evaluate()) {
                return tqr.next().getBinding("setting").getValue().stringValue();
            }
        }
    }

    private String getSettingFromFile(String settingName) throws IOException {
        Path settingsFile = Paths.get(((OwlimSchemaRepository)((SailRepository)repository).getSail()).getStorageFolder(), "GeoSPARQL", "v2", "config.properties");
        Properties properties = new Properties();
        try (FileReader reader = new FileReader(settingsFile.toFile())) {
            properties.load(reader);
        }
        String value = properties.getProperty(settingName);
        return value == null ? null : value.toLowerCase();
    }

    private void assertSetting(String settingNameIRI, String settingNameFile, String settingValue) throws IOException {
        assertEquals(settingValue, getSetting(settingNameIRI));
        if (settingNameFile != null) {
            assertEquals(settingValue, getSettingFromFile(settingNameFile));
        }
    }

    private void setSetting(String settingName, String settingValue) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(String.format("PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" +
                    "INSERT DATA { _:s :%s '''%s''' }", settingName, settingValue)).execute();
        }
    }

	private void setMultipleSettings(String settingName, String settingValue, String settingName1, String settingValue1) {
		try (RepositoryConnection connection = repository.getConnection()) {
			connection.prepareUpdate(String.format("PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" +
					"INSERT DATA { _:s :%s '''%s'''; :%s '''%s'''. }", settingName, settingValue, settingName1, settingValue1)).execute();
		}
	}

    private long getIndexSizeOnDisk() throws IOException {
        Path indexDir = Paths.get(((OwlimSchemaRepository)((SailRepository)repository).getSail()).getStorageFolder(), "GeoSPARQL", "v2", "index");
        AtomicLong size = new AtomicLong(0);
        if (Files.isDirectory(indexDir)) {
            Files.walkFileTree(indexDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    size.addAndGet(Files.size(file));
                    return super.visitFile(file, attrs);
                }
            });
        }

        return size.get();
    }

    @Test
    public void testDefaultQuadPrefixTree() throws Exception {
        // Initially no config file exists so no checks there
        assertSetting("prefixTree", null, "quad");
        assertSetting("currentPrefixTree", null, "quad");

        // Enabling forces indexing and saving the settings to file
        enablePlugin();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        setSetting("prefixTree", "geohash");
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");
    }

    @Test
    public void testSetGeohashPrefixTree() throws Exception {
        // Initially no config file exists so no checks there
        assertSetting("prefixTree", null, "quad");
        assertSetting("currentPrefixTree", null, "quad");

        // Setting tree to geohash, only prefixTree should be affected. Settings are written to file.
        setSetting("prefixTree", "geohash");
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        // Enable plugin forces reindex so currentPrefixTree is updated
        enablePlugin();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "geohash");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "geohash");

        setSetting("prefixTree", "quad");
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "geohash");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "geohash");
    }

    @Test
    public void testSetQuadPrefixTree() throws Exception {
        // Initially no config file exists so no checks there
        assertSetting("prefixTree", null, "quad");
        assertSetting("currentPrefixTree", null, "quad");

        // Setting tree to quad, settings are written to file.
        setSetting("prefixTree", "quad");
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        enablePlugin();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        setSetting("prefixTree", "geohash");
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");
    }

    @Test
    public void testChangeTreeAndRebuild() throws Exception {
        // Initially no config file exists so no checks there
        assertSetting("prefixTree", null, "quad");
        assertSetting("currentPrefixTree", null, "quad");

        setSetting("prefixTree", "quad");
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        enablePlugin();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "quad");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");

        long sizeWithQuad = getIndexSizeOnDisk();

        setSetting("prefixTree", "geohash");
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "quad");
        forceReindex();

        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "geohash");

        restartRepository();
        assertQuery();
        assertSetting("prefixTree", "prefixtree", "geohash");
        assertSetting("currentPrefixTree", "prefixtree.current", "geohash");

        long sizeWithGeohash = getIndexSizeOnDisk();

        assertTrue("quad index size should be smaller than geohash index size",
                sizeWithQuad < sizeWithGeohash);
    }

    @Test
    public void testSetPrecision() throws Exception {
        // This test verifies the default precision and that it can be changed

        // Initially no config file exists so no checks there
        assertSetting("precision", null, "11");
        assertSetting("currentPrecision", null, "11");

        setSetting("precision", "20");
        assertSetting("precision", "precision", "20");
        assertSetting("currentPrecision", "precision.current", "11");

        enablePlugin();
        assertQuery();
        assertSetting("precision", "precision", "20");
        assertSetting("currentPrecision", "precision.current", "20");

        restartRepository();
        assertQuery();
        assertSetting("precision", "precision", "20");
        assertSetting("currentPrecision", "precision.current", "20");

        long sizeWith20 = getIndexSizeOnDisk();

        setSetting("precision", "15");
        assertQuery();
        assertSetting("precision", "precision", "15");
        assertSetting("currentPrecision", "precision.current", "20");

        restartRepository();
        assertQuery();
        assertSetting("precision", "precision", "15");
        assertSetting("currentPrecision", "precision.current", "20");
        forceReindex();
        assertQuery();
        assertSetting("precision", "precision", "15");
        assertSetting("currentPrecision", "precision.current", "15");

        long sizeWith15 = getIndexSizeOnDisk();

        assertTrue("index with smaller precision should be smaller than index with higher precisioen",
                sizeWith15 < sizeWith20);
    }

    @Test
    public void testThrowPluginExceptionOnInvalidPrecisionAndGEOHASHPrefixTree() {
		setSetting("prefixTree", "geohash");
        try {
			setSetting("precision", "25");
            fail("Should throw PluginException, which will be wrapped to RepositoryException");
        } catch (Exception e) {
            assertThat(e.getMessage(),
                    CoreMatchers.containsString("GEOHASH prefix tree requires precision values between 1 and " + GeohashPrefixTree.getMaxLevelsPossible()));
        }
    }

    @Test
    public void testThrowPluginExceptionOnInvalidPrecisionAndQuadPrefixTree() {
		setSetting("prefixTree", "quad");
        try {
			setSetting("precision", "51");
            fail("Should throw PluginException, which will be wrapped to RepositoryException");
        } catch (Exception e) {
            assertThat(e.getMessage(),
                    CoreMatchers.containsString("QUAD prefix tree requires precision values between 1 and " + QuadPrefixTree.MAX_LEVELS_POSSIBLE));
        }
    }

    @Test
    public void testThrowPluginExceptionOnStoredInvalidPrecisionAndGEOHASHPrefixTree() {
		setSetting("precision", "25");
        try {
			setSetting("prefixTree", "geohash");
            fail("Should throw PluginException, which will be wrapped to RepositoryException");
        } catch (Exception e) {
            assertThat(e.getMessage(),
                    CoreMatchers.containsString("GEOHASH prefix tree requires precision values between 1 and " + GeohashPrefixTree.getMaxLevelsPossible()));
        }
    }

    @Test
    public void testThrowPluginExceptionOnNegativePrecision() {
        try {
			setSetting("precision", "-1");
            fail("Should throw PluginException, which will be wrapped to RepositoryException");
        } catch (Exception e) {
            assertThat(e.getMessage(),
                    CoreMatchers.containsString("QUAD prefix tree requires precision values between 1 and " + QuadPrefixTree.MAX_LEVELS_POSSIBLE));
        }
    }

	@Test
	public void shouldProperlySetMultipleSettings() {
		try {
			setMultipleSettings("prefixTree", "quad", "precision", "25");
			setMultipleSettings("prefixTree", "geohash", "precision", "20");

			assertSetting("prefixTree", "prefixtree", "geohash");
			assertSetting("precision", "precision", "20");
		} catch (Exception e) {
			fail("Should properly change prefix tree from quad with higher precision to geohash with correct one");
		}
	}
}
