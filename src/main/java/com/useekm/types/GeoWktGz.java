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
package com.useekm.types;

import com.useekm.indexing.GeoConstants;
import com.useekm.types.exception.InvalidGeometryException;
import org.locationtech.jts.geom.Geometry;
import org.eclipse.rdf4j.model.IRI;

public class GeoWktGz extends AbstractGeo {
    public GeoWktGz(String value) {
        super(value);
    }

    public GeoWktGz(Geometry g) {
        super(WktGzSerializer.INTANCE.toLiteral(g));
    }

    @Override public IRI getType() {
        return GeoConstants.XMLSCHEMA_SPATIAL_TEXTGZ;
    }

    @Override public Geometry getGeo() throws InvalidGeometryException {
        return WktGzSerializer.INTANCE.toGeometry(getValue());
    }
}