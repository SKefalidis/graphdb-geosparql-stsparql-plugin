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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * From two geometries, this function computes the points making up the first geometry that do not make up the other.
 * 
 * @see Geometry#difference(Geometry)
 */
public class SymDifference extends AbstractLiteralBinaryFunction implements GeometryFunction {
    @Override public String getURI() {
        return GeoConstants.GEOF_SYM_DIFFERENCE.stringValue();
    }

    @Override protected Literal evaluate(ValueFactory valueFactory, IRI geotype, Geometry geom1, Geometry geom2, Value... originals) throws ValueExprEvaluationException {
        return GeoConvert.toLiteralExpr(valueFactory, geom1.symDifference(geom2), geotype);
    }
}