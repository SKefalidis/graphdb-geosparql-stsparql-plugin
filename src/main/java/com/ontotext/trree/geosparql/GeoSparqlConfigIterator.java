package com.ontotext.trree.geosparql;

import com.ontotext.trree.sdk.Entities;
import com.ontotext.trree.sdk.StatementIterator;
import org.eclipse.rdf4j.model.Value;

import java.util.*;

/**
 * Iterator that provides access to the plugin's status.
 */
class GeoSparqlConfigIterator extends StatementIterator {
    private GeoSparqlPlugin plugin;
    private Entities entities;

    private long predicates[];
    private int index;

    GeoSparqlConfigIterator(GeoSparqlPlugin plugin, long predicate, Entities entities) {
        this(plugin, predicate, 0, entities);
    }

    GeoSparqlConfigIterator(GeoSparqlPlugin plugin, long predicate, long object, Entities entities) {
        this.plugin = plugin;
        this.entities = entities;

        if (predicate == 0) {
            if (object == 0) {
                predicates = new long[]{plugin.enabledPredicateId, plugin.prefixTreePredicateId,
                        plugin.precisionPredicateId, plugin.currentPrefixTreePredicateId,
                        plugin.currentPrecisionPredicateId, plugin.maxBufferedDocsPredicateId,
                        plugin.ramBufferSizePredicateId, plugin.ignoreErrorsPredicateId};
            } else {
                predicates = getPredicateFromObjectValue(entities.get(object));
            }
        } else {
            predicates = new long[]{predicate};
        }

        subject = plugin.contextId;
    }

    @Override
    public boolean next() {
        if (index < predicates.length) {
            predicate = predicates[index];

            if (predicate == plugin.enabledPredicateId) {
                object = entities.put(GeoSparqlPlugin.VALUE_FACTORY.createLiteral(plugin.getConfig().isEnabled()),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.prefixTreePredicateId) {
                object = entities.put(plugin.getConfig().getPrefixTree().toLiteral(),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.precisionPredicateId) {
                object = entities.put(GeoSparqlPlugin.VALUE_FACTORY.createLiteral(plugin.getConfig().getPrecision()),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.currentPrefixTreePredicateId) {
                object = entities.put(plugin.getConfig().getCurrentPrefixTree().toLiteral(),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.currentPrecisionPredicateId) {
                object = entities.put(GeoSparqlPlugin.VALUE_FACTORY.createLiteral(plugin.getConfig().getCurrentPrecision()),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.maxBufferedDocsPredicateId) {
                object = entities.put(GeoSparqlPlugin.VALUE_FACTORY.createLiteral(plugin.getConfig().getMaxBufferedDocs()),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.ramBufferSizePredicateId) {
                object = entities.put(GeoSparqlPlugin.VALUE_FACTORY.createLiteral(plugin.getConfig().getRamBufferSizeMb()),
                        Entities.Scope.REQUEST);
            } else if (predicate == plugin.ignoreErrorsPredicateId) {
                object = entities.put(GeoSparqlPlugin.VALUE_FACTORY.createLiteral(plugin.getConfig().isIgnoreErrors()),
                        Entities.Scope.REQUEST);
            }

            index++;

            return true;
        }
        return false;
    }

    @Override
    public void close() {
        // no-op
    }

    private long[] getPredicateFromObjectValue(Value objectValue) {
        List<Long> predicatesList = new ArrayList<>();
        for (Map.Entry<Object, Object> propertiesValues : plugin.getConfig().getAsProperties().entrySet()) {
            if (objectValue.stringValue().equalsIgnoreCase(propertiesValues.getValue().toString())) {
                predicatesList.add(getPluginPredicateFromConfigKey(propertiesValues.getKey().toString()));
            }
        }

        return predicatesList.stream().mapToLong(i->i).toArray();
    }

    private long getPluginPredicateFromConfigKey(String propertyKey) {
        Map<String, Long> mappedPropertiesKeyToPluginPredicates = new HashMap<>();
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.ENABLED_KEY, plugin.enabledPredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.CURRENT_PREFIXTREE_KEY, plugin.currentPrefixTreePredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.CURRENT_PRECISION, plugin.currentPrecisionPredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.PREFIXTREE_KEY, plugin.prefixTreePredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.PRECISION_KEY, plugin.precisionPredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.MAX_BUFFERED_DOCS_KEY, plugin.maxBufferedDocsPredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.RAM_BUFFER_SIZE_MB_KEY, plugin.ramBufferSizePredicateId);
        mappedPropertiesKeyToPluginPredicates.put(GeoSparqlConfig.IGNORE_ERRORS_KEY, plugin.ignoreErrorsPredicateId);

        return mappedPropertiesKeyToPluginPredicates.get(propertyKey);
    }
}
