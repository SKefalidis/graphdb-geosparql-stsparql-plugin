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
 * Tests whether a geometry is simple. A geometry is simple if the only self-intersections are at boundary points. See {@link Geometry#isSimple()} for a more formal definition.
 */
public class IsSimple extends AbstractBooleanUnaryFunction {
    @Override public String getURI() {
        return GeoConstants.GEO_IS_SIMPLE.stringValue();
    }

    @Override protected boolean accept(ValueFactory valueFactory, Geometry geom, Value... allArgs) {
        return geom.isSimple();
    }
}