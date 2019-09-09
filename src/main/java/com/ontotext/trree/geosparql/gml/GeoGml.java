package com.ontotext.trree.geosparql.gml;

import com.useekm.indexing.GeoConstants;
import com.useekm.types.AbstractGeo;
import com.useekm.types.exception.InvalidGeometryException;
import org.eclipse.rdf4j.model.IRI;
import org.locationtech.jts.geom.Geometry;

public class GeoGml extends AbstractGeo {
    protected GeoGml(String value) {
        super(value);
    }

    public GeoGml(Geometry geometry) {
        super(GmlSerializer.INSTANCE.toLiteral(geometry));
    }

    @Override
    public IRI getType() {
        return GeoConstants.GEO_GML_LITERAL;
    }

    @Override
    public Geometry getGeo() throws InvalidGeometryException {
        return GmlSerializer.INSTANCE.toGeometry(getValue());
    }

    @Override
    public String toString() {
        return getValue();
    }
}
