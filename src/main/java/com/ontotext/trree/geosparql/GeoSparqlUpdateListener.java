package com.ontotext.trree.geosparql;

import com.ontotext.trree.geosparql.util.GeoSparqlUtils;
import com.ontotext.trree.sdk.*;
import org.locationtech.jts.geom.Geometry;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongProcedure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Listener for incremental indexing of GeoSPARQL data.
 */
class GeoSparqlUpdateListener implements ParallelTransactionListener, StatementListener {
	private final GeoSparqlPlugin parent;
	private final long asWKT;
	private final long asGML;
	private final long hasDefaultGeometry;

	private TLongHashSet geometriesToUpdate = new TLongHashSet();
	private TLongHashSet featuresToUpdate = new TLongHashSet();

	GeoSparqlUpdateListener(GeoSparqlPlugin parent, long asWKT, long asGML, long hasDefaultGeometry) {
		this.parent = parent;
		this.asWKT = asWKT;
		this.asGML = asGML;
		this.hasDefaultGeometry = hasDefaultGeometry;
	}

	@Override
	public boolean statementAdded(long subject, long predicate, long object, long context, boolean explicit,
								  PluginConnection pluginConnection) {
		if (! parent.getConfig().isEnabled()) {
			return false;
		}

		if (predicate == asWKT || predicate == asGML) {
			geometriesToUpdate.add(subject);
		} else if (predicate == hasDefaultGeometry) {
			featuresToUpdate.add(subject);
		}
		return false;
	}

	@Override
	public boolean statementRemoved(long subject, long predicate, long object, long context, boolean explicit,
									PluginConnection pluginConnection) {
		if (! parent.getConfig().isEnabled()) {
			return false;
		}

		if (predicate == asWKT || predicate == asGML) {
			geometriesToUpdate.add(subject);
		} else if (predicate == hasDefaultGeometry) {
			featuresToUpdate.add(subject);
		}
		return false;
	}

	@Override
	public void transactionStarted(PluginConnection pluginConnection) {
		parent.tmpPrefixTree = null;
		parent.tmpPrecision = 0;
		if (! parent.getConfig().isEnabled()) {
			return;
		}

		try {
			parent.indexer.begin();
		} catch (Exception e) {
			throw new PluginException("Unable to start indexer transaction.", e);
		}
	}

	@Override
	public void transactionCommit(PluginConnection pluginConnection) {
		// In case of changed PrefixTree or precision in GeoSparqlPlugin should validate provided
		// parameters and change them respectively. Note that user could change both values or only single one.
		// In secondary case we'll validate changed value and already set value
		if (parent.tmpPrefixTree != null || parent.tmpPrecision != 0) {
			GeoSparqlConfig config = parent.getConfig();
			GeoSparqlConfig.PrefixTree prefixTree = parent.tmpPrefixTree != null ? parent.tmpPrefixTree : config.getPrefixTree();
			int precision = parent.tmpPrecision != 0 ? parent.tmpPrecision : config.getPrecision();
			GeoSparqlUtils.validateParams(prefixTree, precision);
			config.setPrefixTree(prefixTree);
			config.setPrecision(precision);
			GeoSparqlUtils.saveConfig(config, parent.getDataDir().toPath());
		}

		if (! parent.getConfig().isEnabled()) {
		    return;
		}

		final TLongHashSet processedFeatures = new TLongHashSet();

		final Function<Long, String> subjectMapper = (subject) -> pluginConnection.getEntities().get(subject).stringValue();

		geometriesToUpdate.forEach(new TLongProcedure() {
			final List<Geometry> geometries = new ArrayList<Geometry>();

			void processGeometryWithPredicate(long value, long predicate) {
				StatementIterator sit = pluginConnection.getStatements().get(value, predicate, 0);
				try {
					while (sit.next()) {
						Geometry geometry = parent.getGeometryFromLiteralId(sit.object, predicate, pluginConnection.getEntities());
						if (geometry != null) {
							geometries.add(geometry);
						}
					}
				} finally {
					sit.close();
				}
			}

			@Override
			public boolean execute(long value) {
				{
					// Index the geometry
					processGeometryWithPredicate(value, asWKT);
					processGeometryWithPredicate(value, asGML);
					parent.indexer.indexGeometryList(value, subjectMapper, geometries);
				}

				{
					// Add each feature and reuse the geometries we already identified
					StatementIterator sit = pluginConnection.getStatements().get(0, hasDefaultGeometry, value);
					try {
						while (sit.next()) {
							parent.indexer.indexGeometryList(sit.subject, subjectMapper, geometries);
							processedFeatures.add(sit.subject);
						}
					} finally {
						sit.close();
					}
				}

				geometries.clear();

				return true;
			}
		});

		featuresToUpdate.forEach(new TLongProcedure() {
			final List<Geometry> geometries = new ArrayList<Geometry>();

			void processGeometryWithPredicate(long value, long predicate) {
				StatementIterator sit = pluginConnection.getStatements().get(value, predicate, 0);
				try {
					while (sit.next()) {
						Geometry geometry = parent.getGeometryFromLiteralId(sit.object, predicate, pluginConnection.getEntities());
						if (geometry != null) {
							geometries.add(geometry);
						}
					}
				} finally {
					sit.close();
				}
			}
			void processFeatureWithPredicate(long value, long predicate) {
				StatementIterator sit = pluginConnection.getStatements().get(value, hasDefaultGeometry, 0);
				try {
					while (sit.next()) {
						processGeometryWithPredicate(sit.object, predicate);
					}
				} finally {
					sit.close();
				}
			}

			@Override
			public boolean execute(long value) {

				// unless we already processed that feature as part of the geometries update
				if (!processedFeatures.contains(value)) {
					processFeatureWithPredicate(value, asWKT);
					processFeatureWithPredicate(value, asGML);
					parent.indexer.indexGeometryList(value, subjectMapper, geometries);
				}

				geometries.clear();

				return true;
			}
		});

		cleanupAfterTransaction();

		try {
			parent.indexer.commit();
		} catch (Exception e) {
			parent.getLogger().warn("Unable to commit indexer transaction.", e);
		}
	}

    @Override
    public void transactionCompleted(PluginConnection pluginConnection) {

    }

    @Override
	public void transactionAborted(PluginConnection pluginConnection) {
		if (! parent.getConfig().isEnabled()) {
			return;
		}

		cleanupAfterTransaction();
		try {
			parent.indexer.rollback();
		} catch (Exception e) {
			parent.getLogger().warn("Unable to rollback indexer transaction.", e);
		}
	}

	private void cleanupAfterTransaction() {
		// TODO: clear() or new object?
		geometriesToUpdate.clear();
		featuresToUpdate.clear();
	}
}
