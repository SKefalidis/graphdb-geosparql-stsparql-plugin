package com.ontotext.trree.geosparql;

import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator implementation for a single entity and a static list of geometries.
 */
public class SingleEntityGeometryIterator implements EntityGeometryIterator {
	private final long entityId;
	private final Iterator<Geometry> iGeometries;
	private Geometry geometry;

	public SingleEntityGeometryIterator(long entityId, List<Geometry> geometries) {
		this.entityId = entityId;
		this.iGeometries = geometries != null ? geometries.iterator() : Collections.<Geometry>emptyIterator();
	}

	public SingleEntityGeometryIterator(long entityId, Geometry geometry) {
		this.entityId = entityId;
		this.iGeometries = geometry != null ? Collections.singletonList(geometry).iterator() : Collections.<Geometry>emptyIterator();
	}


	@Override
	public long getEntityForLastGeometry() {
		return entityId;
	}

	@Override
	public Geometry nextGeometry() {
		return geometry = iGeometries.next();
	}

	@Override
	public Geometry lastGeometry() {
		return geometry;
	}

	@Override
	public boolean hasNextGeometry() {
		return iGeometries.hasNext();
	}

	@Override
	public void advanceToNextEntity() {

	}

	@Override
	public void close() throws IOException {
		// does nothing
	}
}
