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

import com.useekm.types.GeoConvert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFunction implements Function {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFunction.class);

    protected Geometry asGeometry(Value value) throws ValueExprEvaluationException {
        if (!(value instanceof Literal))
            throw new ValueExprEvaluationException("expected literal, found " + value.toString());
        return GeoConvert.toGeometryExpr((Literal)value, true);
    }

    protected double asDouble(Value value) throws ValueExprEvaluationException {
        if (!(value instanceof Literal))
            throw new ValueExprEvaluationException("expected xsd:double, found " + value.toString());
        try {
            return ((Literal)value).doubleValue();
        } catch (NumberFormatException e) {
            throw new ValueExprEvaluationException(e);
        }
    }
    
    /**
     * Returns the result of the function evaluation.
     * 
     * @throws ValueExprEvaluationException For illegal or incompatible arguments.
     * @throws TopologyException For invalid input geometries. This will raise a {@link ValueExprEvaluationException} for the filter evaluation.
     */
    public abstract Value eval(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException, TopologyException;

    @Override public final Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        try {
            return eval(valueFactory, args);
        } catch (TopologyException e) {
            //handle invalid geometries just like type errors
            LOG.debug("Raising filter evaluation error due to invalid input geometry", e);
            throw new ValueExprEvaluationException(e);
        }
    }
}