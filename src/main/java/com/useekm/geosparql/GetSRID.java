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
import org.locationtech.jts.geom.Geometry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Computes the area of the surface if it is a polygon or multi-polygon, in SRID units.
 */
public class GetSRID extends AbstractLiteralUnaryFunction implements LiteralFunction {
    @Override public String getURI() {
        return GeoConstants.GEOF_GETSRID.stringValue();
    }

    @Override protected Literal evaluate(ValueFactory valueFactory, IRI geotype, Geometry geom) {
        int srid = geom.getSRID();
        if (srid <= 0)
            srid = GeoFactory.getDefaultSrid();
        return valueFactory.createLiteral(geom.getSRID());
        //TODO we should return an URI, not an int (or at least with geosparql:wkt we should return an URI). Maybe for our own types we can return the int??
    }
}