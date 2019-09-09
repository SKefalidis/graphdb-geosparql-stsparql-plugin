package com.ontotext.trree.geosparql;

import com.ontotext.trree.sdk.Entities;
import com.ontotext.trree.sdk.StatementIterator;

/**
 * Iterator that provides access to the plugin's status.
 */
class GeoSparqlConfigIterator extends StatementIterator {
    private GeoSparqlPlugin plugin;
    private Entities entities;

    private long predicates[];
    private int index;

    GeoSparqlConfigIterator(GeoSparqlPlugin plugin, long predicate, Entities entities) {
        this.plugin = plugin;
        this.entities = entities;

        if (predicate == 0) {
            predicates = new long[]{plugin.enabledPredicateId, plugin.prefixTreePredicateId,
                    plugin.precisionPredicateId, plugin.currentPrefixTreePredicateId,
                    plugin.currentPrecisionPredicateId, plugin.maxBufferedDocsPredicateId,
                    plugin.ramBufferSizePredicateId};
        } else {
            predicates = new long[]{predicate};
        }

        subject = plugin.contextId;
        //context = plugin.contextId;
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
}
