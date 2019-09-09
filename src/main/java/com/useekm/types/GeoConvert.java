/*
 * Copyright 2013 by TalkingTrends (Amsterdam, The Netherlands)
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

import com.ontotext.trree.geosparql.conversion.CRSUtil;
import com.ontotext.trree.geosparql.conversion.TransformCoordinateFilter;
import com.ontotext.trree.geosparql.gml.GmlSerializer;
import com.useekm.indexing.GeoFactory;
import com.useekm.types.exception.InvalidGeometryException;
import org.apache.commons.lang.Validate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;

import java.util.HashMap;
import java.util.Map;

public final class GeoConvert {
    private static final AbstractGeoSerializer NO_TYPE_SERIALIZER = new WktSerializer();
    private static final Map<String, AbstractGeoSerializer> GEO_SERIALIZERS = new HashMap<String, AbstractGeoSerializer>();
    static {
        add(WktOgcSerializer.INTANCE);
        add(WktSerializer.INTANCE);
        add(WktGzSerializer.INTANCE);
        add(WkbSerializer.INTANCE);
        add(WkbGzSerializer.INTANCE);
        add(GmlSerializer.INSTANCE);
    }

    private static void add(AbstractGeoSerializer serializer) {
        GEO_SERIALIZERS.put(serializer.getDatatype().stringValue(), serializer);
    }

    private static AbstractGeoSerializer getSerializer(IRI datatype, boolean acceptNoType) throws InvalidGeometryException {
        if (datatype == null || datatype.equals(XMLSchema.STRING)) {
            if (acceptNoType)
                return NO_TYPE_SERIALIZER;
            throw new InvalidGeometryException((IRI)null);
        }
        AbstractGeoSerializer s = GEO_SERIALIZERS.get(datatype.stringValue());
        if (s == null)
            throw new InvalidGeometryException(datatype);
        return s;
    }

    /**
     * @return true if the datatype is a supported geometry serialisation type.
     */
    public static boolean isSupported(IRI datatype) {
        return datatype != null && GEO_SERIALIZERS.containsKey(datatype.stringValue());
    }

    /**
     * Converts the literal to a geometry.
     * 
     * @param literal The literal to convert
     * @param acceptNoType Set to true when literals without a datatype should be interpreted as a WKT geometry serialization
     * 
     * @throws InvalidGeometryException When the type is not a geometry datatype, or the geometry serialization is invalid.
     */
    public static Geometry toGeometry(Literal literal, boolean acceptNoType) throws InvalidGeometryException {
        return getSerializer(literal.getDatatype(), acceptNoType).toGeometry(literal.stringValue());
    }

    /**
     * Exactly equal to {@link #toGeometry(Literal, boolean)}, except it throws {@link ValueExprEvaluationException} instead of {@link InvalidGeometryException}.
     * 
     * To be used during query/expression evaluation.
     */
    public static Geometry toGeometryExpr(Literal literal, boolean acceptNoType) throws ValueExprEvaluationException {
        try {
            return getSerializer(literal.getDatatype(), acceptNoType).toGeometry(literal.stringValue());
        } catch (InvalidGeometryException e) {
            throw new ValueExprEvaluationException(e);
        }
    }

    /**
     * Converts the geometry to a literal of the given type.
     * 
     * @param geometry The geometry to convert
     * @param datatype The datatype
     * 
     * @throws IllegalArgumentException When the datatype is not a supported geometry datatype
     */
    public static Literal toLiteral(ValueFactory vf, Geometry geometry, IRI datatype) {
        AbstractGeoSerializer s = GEO_SERIALIZERS.get(datatype.stringValue());
        Validate.notNull(s);
        return vf.createLiteral(s.toLiteral(geometry), datatype);
    }

    /**
     * Exactly equal to {@link #toLiteral(ValueFactory, Geometry, IRI)}, except it uses the default serializer if no datatype was given.
     * 
     * To be used during query/expression evaluation.
     */
    public static Literal toLiteralExpr(ValueFactory vf, Geometry geometry, IRI datatype) {
        if (datatype == null)
            return vf.createLiteral(NO_TYPE_SERIALIZER.toLiteral(geometry), NO_TYPE_SERIALIZER.getDatatype());
        return toLiteral(vf, geometry, datatype);
    }

    /**
     * Converts the literal to an {@link AbstractGeo} instance (the geometry datatype used with Alibaba). If the datatype of the literal is not a geometry type this method throws
     * {@link InvalidGeometryException}. The literal itself is not parsed/checked for validity.
     * 
     * @param literal The literal to convert
     * 
     * @throws InvalidGeometryException When the type is not a geometry datatype.
     */
    public static AbstractGeo toGeo(Literal literal) throws InvalidGeometryException {
        return getSerializer(literal.getDatatype(), false).toGeo(literal.stringValue());
    }

    public static Geometry wktToGeometry(String value) throws InvalidGeometryException {
        int startOfUri = value.indexOf("<");
        for (int i = 0; i < startOfUri; i++) {
            if (value.charAt(i) > 32) {
                // text before start of uri contains a printable character, assume this is not a uri
                startOfUri = -1;
                break;
            }
        }

        MathTransform mathTransform = null;
        if (startOfUri >= 0) {
            // value contains a CRS uri
            int endOfUri = value.indexOf(">");
            if (endOfUri > 0) {
                final String uri = value.substring(startOfUri + 1, endOfUri);
                if (!uri.equals("http://www.opengis.net/def/crs/OGC/1.3/CRS84")) {
                    String shortId = CRSUtil.crsUriToShortId(uri);
                    if (shortId != null) {
                        try {
                            mathTransform = CRSUtil.findMathTransform(shortId, CRSUtil.TARGET_CRS);
                        } catch (FactoryException e) {
                            throw new InvalidGeometryException("Unable to find SRS", e);
                        }
                    }
                }
                value = value.substring(endOfUri + 1);
            } else {
                throw new InvalidGeometryException("CRS uri in value is incomplete: " + value, null);
            }
        }
        WKTReader reader = new WKTReader(GeoFactory.getDefaultGeometryFactory());
        try {
            Geometry g = reader.read(value);
            if (mathTransform != null) {
                g.apply(new TransformCoordinateFilter(mathTransform));
                g.setSRID(4326);
            }
            return g;
        } catch (ParseException e) {
            throw new InvalidGeometryException("Invalid geo WKT: " + value, e);
        }
    }

    /**
     * For test cases
     */
    public static Literal toLiteral(ValueFactory vf, String value, IRI datatype) {
        try {
            Geometry geometry = wktToGeometry(value);
            return toLiteral(vf, geometry, datatype);
        } catch (InvalidGeometryException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Geometry getEmptyGeometry() {
        return GeoFactory.getDefaultGeometryFactory().createPoint((CoordinateSequence)null);
    }
}
