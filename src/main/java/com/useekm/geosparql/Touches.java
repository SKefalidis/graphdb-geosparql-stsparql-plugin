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
import org.locationtech.jts.geom.Geometry;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Tests whether the first geometry touches the second geometry. A geometry geom1 touches geometry geom2 if they have at least one point in common, but their interiors do not
 * intersect. Two geometries that have dimension 0 do not touch.
 * 
 * @see Geometry#touches(Geometry)
 */
public class Touches extends AbstractBooleanBinaryFunction {
    @Override public String getURI() {
        return GeoConstants.GEOF_SF_TOUCHES.stringValue();
    }

    /**
     * @return a boolean Literal that is true if geom1 touches geom2.
     */
    @Override protected boolean accept(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... originals) {
        return geom1.touches(geom2);
    }
}
