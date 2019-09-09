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
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Computes a simplified version of the given geometry using the Douglas-Peuker algorithm.
 * Will avoid creating derived geometries (polygons in particular) that are invalid.
 * 
 * @see TopologyPreservingSimplifier
 */
public class SimplifyPreserveTopology extends AbstractGeomGeomDoubleFunction {
    @Override public String getURI() {
        return GeoConstants.EXT_SIMPLIFY_PRESERVE_TOPOLOGY.stringValue();
    }

    @Override protected Literal evaluate(ValueFactory valueFactory, IRI geotype, Geometry geom, double distanceTolerance) {
        return GeoConvert.toLiteral(valueFactory, TopologyPreservingSimplifier.simplify(geom, distanceTolerance), geotype);
    }
}