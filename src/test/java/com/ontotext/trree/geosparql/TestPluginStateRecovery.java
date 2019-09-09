package com.ontotext.trree.geosparql;

import com.ontotext.trree.geosparql.util.GeoSparqlUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 01 Oct 2015.
 */
public class TestPluginStateRecovery extends AbstractGeoSparqlPluginTest {
    private boolean isPluginEnabled(File pluginDataDir) {
        return GeoSparqlUtils.readConfig(pluginDataDir.toPath()).isEnabled();
    }

    @Test
    public void testPluginStateEnabled() throws Exception {
        enablePlugin();
        assertTrue(Files.isReadable(GeoSparqlConfig.resolveConfigPath(getGeoSparqlStorageDir().toPath())));
        assertTrue(isPluginEnabled(getGeoSparqlStorageDir()));
    }

    @Test
    public void testPluginStateDisabled() {
        assertFalse(Files.isReadable(GeoSparqlConfig.resolveConfigPath(getGeoSparqlStorageDir().toPath())));
        assertFalse(isPluginEnabled(getGeoSparqlStorageDir()));
    }

    @Test
    public void testPluginStateDisabledAfterEnabled() throws Exception {
        enablePlugin();
        disablePlugin();
        assertTrue(Files.isReadable(GeoSparqlConfig.resolveConfigPath(getGeoSparqlStorageDir().toPath())));
        assertFalse(isPluginEnabled(getGeoSparqlStorageDir()));
    }
}
