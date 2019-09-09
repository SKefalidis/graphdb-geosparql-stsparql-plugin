package com.ontotext.trree.geosparql.lucene;

import com.ontotext.trree.geosparql.EntityGeometryIterator;
import com.ontotext.trree.geosparql.GeoSparqlIndexer;
import org.locationtech.jts.geom.Geometry;
import org.apache.lucene.spatial.query.SpatialOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes an EntityGeometryIterator and looks up each geometry from it in Lucene with the provided SpatialOperation.
 */
public class LuceneMultiSearchEntityGeometryIterator implements EntityGeometryIterator {
    private GeoSparqlIndexer indexer;
    private SpatialOperation spatialOperation;
    private EntityGeometryIterator luceneIterator;
    private List<EntityGeometryIterator> iteratorsToClose = new ArrayList<>();

    public LuceneMultiSearchEntityGeometryIterator(GeoSparqlIndexer indexer, SpatialOperation spatialOperation) {
        this.indexer = indexer;
        this.spatialOperation = spatialOperation;
    }

    @Override
    public long getEntityForLastGeometry() {
        return luceneIterator == null ? 0 : luceneIterator.getEntityForLastGeometry();
    }

    @Override
    public Geometry nextGeometry() {
        return luceneIterator == null ? null : luceneIterator.nextGeometry();
    }

    @Override
    public Geometry lastGeometry() {
        return luceneIterator == null ? null : luceneIterator.lastGeometry();
    }

    @Override
    public boolean hasNextGeometry() {
        return luceneIterator != null && luceneIterator.hasNextGeometry();
    }

    @Override
    public void advanceToNextEntity() {
        if (luceneIterator == null) {
            return;
        }

        luceneIterator.advanceToNextEntity();
    }

    @Override
    public void close() throws IOException {
        for(EntityGeometryIterator itty : iteratorsToClose) {
            itty.close();
        }
    }

    public void search(Geometry geometry) {
        luceneIterator = indexer.getMatchingObjects(geometry, spatialOperation);
        iteratorsToClose.add(luceneIterator);
    }
}
