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
import com.useekm.types.GeoConvert;
import org.locationtech.jts.geom.Geometry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Computes a geometry that represents all points whose distance from this Geometry is less than or equal to distance. The buffer of a Geometry is the Minkowski sum or difference
 * of the geometry with a disc of radius <code>abs(distance)</code>.
 * <p>
 * The buffer operation always returns a polygonal result. The negative or zero-distance buffer of lines and points is always an empty polygon. This is also the result for the
 * buffers of degenerate (zero-area) polygons.
 * 
 * @see Geometry#buffer(double)
 */

public class Buffer extends AbstractGeomGeomDoubleFunction {
    @Override public String getURI() {
        return GeoConstants.GEOF_BUFFER.stringValue();
    }

    @Override protected Literal evaluate(ValueFactory valueFactory, IRI geotype, Geometry geom, double distance) {
        return GeoConvert.toLiteral(valueFactory, geom.buffer(distance), geotype);
    }
}