package com.ontotext.trree.geosparql.conversion;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Filter to convert coordinates from one CRS to another.
 */
public class TransformCoordinateFilter implements CoordinateFilter {
	final double[] srcPoints = new double[2];
	final double[] targetPoints = new double[2];
	final MathTransform mathTransform;

	public TransformCoordinateFilter(MathTransform mathTransform) {
		this.mathTransform = mathTransform;
	}

	@Override
	public void filter(Coordinate coord) {
		try {
			srcPoints[0] = coord.x;
			srcPoints[1] = coord.y;
			mathTransform.transform(srcPoints, 0, targetPoints, 0, 1);
			coord.x = targetPoints[0];
			coord.y = targetPoints[1];
		} catch (TransformException e) {
			// TODO: what do we do on exception?
			e.printStackTrace();
		}
	}
}
