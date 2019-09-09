package com.ontotext.trree.geosparql;

import com.useekm.geosparql.Distance;
import com.useekm.indexing.GeoFactory;
import com.useekm.types.exception.InvalidGeometryException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Assert;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.junit.Test;

/**
 * Created by avataar on 09.04.2015..
 */
public class EntityGeometryIteratorTest {
	@Test
	public void testSer() throws InvalidGeometryException, ParseException, ValueExprEvaluationException {
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 1);
		WKTReader reader = new WKTReader(GeoFactory.getDefaultGeometryFactory());

		Geometry g1 = reader.read("point(0 0)");
		Geometry g2 = reader.read("point(0 10)");
		Geometry l1 = reader.read("linestring(10 0, 10 0)");

		final Literal literal = new Distance().computeDistanceinUnits(
				SimpleValueFactory.getInstance(), g1, l1, null, null,
				SimpleValueFactory.getInstance().createIRI("http://www.opengis.net/def/uom/OGC/1.0/metre"));

        Assert.equals(10.0, g2.distance(g1));
        Assert.equals(10.0, g1.distance(l1));
        Assert.equals(14.142135623730951, g2.distance(l1));
		Assert.equals(1113194.9079327357, literal.doubleValue());
        Assert.equals(SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2001/XMLSchema#double"), literal.getDatatype());
	}
}
