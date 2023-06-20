package com.ontotext.trree.geosparql.conversion;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import tech.units.indriya.function.Calculus;
import tech.units.indriya.function.DefaultNumberSystem;

/**
 * Misc utility methods for CRSs.
 */
public class CRSUtil {
	public static final String TARGET_CRS = "CRS:84";

	private static final String EPSG_PREFIX = "http://www.opengis.net/def/crs/EPSG/0/";
	private static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

	// Explicit instantiation of the class using the plugin's class loader.
	// Ensures consistent loading and eliminates conflicts or inconsistencies caused by different class loaders.
	static {
		Calculus.setCurrentNumberSystem(new DefaultNumberSystem());
	}

	public static String crsUriToShortId(String uri) {
		if (CRS84.equals(uri)) {
			return TARGET_CRS;
		} else {
			if (uri.startsWith(EPSG_PREFIX)) {
				return "EPSG:" + uri.substring(EPSG_PREFIX.length());
			}
		}

		return null;
	}

	public static MathTransform findMathTransform(String sourceCRS, String targetCRS) throws FactoryException {
		final CoordinateReferenceSystem src = CRS.decode(sourceCRS);
		final CoordinateReferenceSystem dst = CRS.decode(targetCRS);

		if (!src.equals(dst)) {
			return CRS.findMathTransform(src, dst);
		} else {
			return null;
		}
	}
}
