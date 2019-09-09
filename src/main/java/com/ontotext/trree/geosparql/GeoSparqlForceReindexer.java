package com.ontotext.trree.geosparql;

import com.ontotext.trree.sdk.PluginConnection;
import com.ontotext.trree.sdk.StatementIterator;
import org.locationtech.jts.geom.Geometry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reindexing all indexable data.
 */
public class GeoSparqlForceReindexer {
	private final GeoSparqlIndexer indexer;
	private final GeoSparqlPlugin plugin;

	public GeoSparqlForceReindexer(GeoSparqlIndexer indexer, GeoSparqlPlugin plugin) {
		this.indexer = indexer;
		this.plugin = plugin;
	}

	public void reindex(PluginConnection pluginConnection) throws Exception {
		indexer.freshIndex();
		reindexGeometries(pluginConnection);
		reindexFeatures(pluginConnection);
	}

	private void reindexGeometries(PluginConnection pluginConnection) {
		class Processor {
			String mapSubject(Long subject) {
				return pluginConnection.getEntities().get(subject).stringValue();
			}

			void process(long predicate) {
				StatementIterator geoSerItty = pluginConnection.getStatements().get(0, predicate, 0);
				try {
					while (geoSerItty.next()) {
						Geometry g = plugin.getGeometryFromLiteralId(geoSerItty.object, predicate,
								pluginConnection.getEntities());
						if (g != null) {
							indexer.indexGeometry(geoSerItty.subject, this::mapSubject, g);
						}
					}
				} finally {
					geoSerItty.close();
				}
			}
		}

		Processor p = new Processor();

		p.process(plugin.asWKT);
		p.process(plugin.asGML);
	}

	private void reindexFeatures(PluginConnection pluginConnection) {
		class Processor {
			String mapSubject(Long subject) {
				return pluginConnection.getEntities().get(subject).stringValue();
			}

			void process(long feature, long subject, long predicate) {
				StatementIterator geoSerItty = pluginConnection.getStatements().get(subject, predicate, 0);
				try {
					while (geoSerItty.next()) {
						Geometry g = plugin.getGeometryFromLiteralId(geoSerItty.object, predicate,
								pluginConnection.getEntities());
						if (g != null) {
							indexer.indexGeometry(feature, this::mapSubject, g);
						}
					}
				} finally {
					geoSerItty.close();
				}
			}
		}

		Processor p = new Processor();

		StatementIterator defGeoItty = pluginConnection.getStatements().get(0, plugin.hasDefaultGeometry, 0);
		try {
			while (defGeoItty.next()) {
				p.process(defGeoItty.subject, defGeoItty.object, plugin.asWKT);
				p.process(defGeoItty.subject, defGeoItty.object, plugin.asGML);
			}
		} finally {
			defGeoItty.close();
		}
	}
}
