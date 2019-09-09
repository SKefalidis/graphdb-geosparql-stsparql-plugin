/*
 * Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensahara.com/licenses/apache-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.useekm.geosparql;

import com.useekm.indexing.GeoConstants;
import com.useekm.indexing.GeoFactory;
import com.useekm.types.GeoConvert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Function that computes for two given geometries: the point on the first geometry that is closest to the second geometry.
 */
public class ClosestPoint extends AbstractLiteralBinaryFunction implements GeometryFunction {
    @Override public String getURI() {
        return GeoConstants.EXT_CLOSEST_POINT.stringValue();
    }

    @Override protected Literal evaluate(ValueFactory valueFactory, IRI geotype, Geometry geom1, Geometry geom2, Value... allArgs) {
        DistanceOp distOp = new DistanceOp(geom1, geom2);
        Point result = GeoFactory.getDefaultGeometryFactory().createPoint(distOp.nearestPoints()[0]);
        return GeoConvert.toLiteral(valueFactory, result, geotype);
    }
}