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

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.locationtech.jts.geom.Geometry;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;

public abstract class AbstractBooleanBinaryFunction extends AbstractBooleanFunction {
    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    protected abstract boolean accept(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... allArgs) throws ValueExprEvaluationException;

    public boolean evaluate(Geometry geom1, Geometry geom2) {
        return accept(VALUE_FACTORY, geom1, geom2);
    }

    @Override public boolean accept(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        if (args.length < 2)
            throw new ValueExprEvaluationException(getURI() + " function expects 2+ arguments, found " + args.length);
        else {
            Geometry geom1 = asGeometry(args[0]);
            Geometry geom2 = asGeometry(args[1]);
            return accept(valueFactory, geom1, geom2, args);
        }
    }
}