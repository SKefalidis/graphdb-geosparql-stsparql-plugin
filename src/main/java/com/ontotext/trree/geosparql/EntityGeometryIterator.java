package com.ontotext.trree.geosparql;

import org.locationtech.jts.geom.Geometry;

import java.io.Closeable;

/**
 * Iterator over entities and geometries. Each entity may have multiple geometries associated with it.
 */
public interface EntityGeometryIterator extends Closeable {
	/**
	 * Returns the entity id of associated with the geometry returned by the last call to nextGeometry().
	 *
	 * @return a GraphDB entity id
	 */
	long getEntityForLastGeometry();

	/**
	 * Returns the next geometry.
	 *
	 * @return a geometry object or null if there aren't more geometries.
	 */
	Geometry nextGeometry();

	/**
	 * Returns the geometry returned by the last call to nextGeometry().
	 *
	 * @return a geometry object or null if nextGeometry() hasn't been called yet.
	 */
	Geometry lastGeometry();

	/**
	 * Checks if there are more geometries.
	 *
	 * @return true if there are more geometries, false otherwise.
	 */
	boolean hasNextGeometry();

	/**
	 * Skips the remaining geometries for the current entity such that hasNextGeometry() and nextGeometry() will use
	 * the next available entity, if any.
	 */
	void advanceToNextEntity();
}
