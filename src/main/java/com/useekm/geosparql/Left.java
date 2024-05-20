/*
 * Copyright 2024 by AI Team, University of Athens
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

public class Left extends AbstractBooleanBinaryFunction {
    @Override public String getURI() {
        return GeoConstants.ST_RDF_LEFT.stringValue();
    }

    /**
     * @return a boolean Literal that is true if the minumum bounding box of geom1 is strictly on the left of the minimum bounding box of geom2.
     */
    @Override protected boolean accept(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... originals) {
        var maxX1 = geom1.getEnvelope().getCoordinates()[2].x;
        var minX2 = geom2.getEnvelope().getCoordinates()[0].x;
        return minX2 > maxX1;
    }
}
