package com.ontotext.trree.geosparql;

import com.useekm.geosparql.*;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.eclipse.rdf4j.model.IRI;
import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

import static com.useekm.indexing.GeoConstants.*;

/**
 * Enum describing all GeoSPARQL functions/relations. All values support evaluation.
 *
 * Each enum value is defined by three arguments:
 * - The class of the corresponding function to evaluate in order to test if the relation holds
 * - The IRI of the predicate to use in SPARQL
 * - The Lucene operation that is compatible (returns exact or broader matches) with this relation
 *
 * For each relation we also have as comments the DE-9IM intersection pattern for the corresponding
 * Lucene operation as well as the pattern of the relation if different from the Lucene one.
 * All uppercase letters and digits in the two patterns are used to determine if there is an exact
 * or broader match. The lowercase letters in the relation pattern are not used in the Lucene matching
 * and indicate that a broader match will be returned.
 *
 */
public enum GeoSparqlFunction {
    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Simple Features Topological Relations //////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    // Equals:      TFFFTFFFT
	SF_EQUALS(Equals.class, GEO_SF_EQUALS, SpatialOperation.IsEqualTo),

    // Disjoint:    FF*FF****
	SF_DISJOINT(Disjoint.class, GEO_SF_DISJOINT, SpatialOperation.IsDisjointTo),

    // Intersects:  T********
    //              *T*******
    //              ***T*****
    //              ****T****
	SF_INTERSECTS(Intersects.class, GEO_SF_INTERSECTS, SpatialOperation.Intersects),

    // Intersects:  *T*******
    //              ***T*****
    //              ****T****
    // Touches:     fT*******
    //              f**T*****
    //              f***T****
	SF_TOUCHES(Touches.class, GEO_SF_TOUCHES, SpatialOperation.Intersects),

    // Within:      T*F**F***
	SF_WITHIN(Within.class, GEO_SF_WITHIN, SpatialOperation.IsWithin),

    // Contains:    T*****FF*
	SF_CONTAINS(Contains.class, GEO_SF_CONTAINS, SpatialOperation.Contains),

    // Overlaps:    T*T***T** (for A/A, P/P)
    //              1*T***T** (for L/L)
	SF_OVERLAPS(Overlaps.class, GEO_SF_OVERLAPS, SpatialOperation.Overlaps),

    // Intersects:  T********
    // Crosses:     T*t***t** (for P/L, P/A, L/A)
    //              0******** (for L/L)
	SF_CROSSES(Crosses.class, GEO_SF_CROSSES, SpatialOperation.Intersects),


    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Egenhofer Topological Relations /////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    // Equals:      TFFFTFFFT
    // EhEquals:    TFFFTFFFT
	EH_EQUALS(EhEquals.class, GEO_EH_EQUALS, SpatialOperation.IsEqualTo),

    // Disjoint:    FF*FF****
    // EhDisjoint:  FF*FF****
	EH_DISJOINT(EhDisjoint.class, GEO_EH_DISJOINT, SpatialOperation.IsDisjointTo),

    // Intersects:  *T*******
    //              ***T*****
    //              ****T****
    // EhMeet:      fT*******
    //              f**T*****
    //              f***T****
	EH_MEET(EhMeet.class, GEO_EH_MEET, SpatialOperation.Intersects),

    // Overlaps:    T*T***T**
    // EhOverlap:   T*T***T**
	EH_OVERLAP(EhOverlap.class, GEO_EH_OVERLAP, SpatialOperation.Overlaps),

    // Contains:    T*****FF*
    // EhCovers:    T*tft*FF*
	EH_COVERS(EhCovers.class, GEO_EH_COVERS, SpatialOperation.Contains),

    // Within:      T*F**F***
    // EhCoveredBy: TfF*tFt**
	EH_COVERED_BY(EhCoveredBy.class, GEO_EH_COVERED_BY, SpatialOperation.IsWithin),

    // Within:      T*F**F***
    // EhInside:    TfF*fFt**
    EH_INSIDE(EhInside.class, GEO_EH_INSIDE, SpatialOperation.IsWithin),

    // Contains:    T*****FF*
    // EhContains:  T*tff*FF*
	EH_CONTAINS(EhContains.class, GEO_EH_CONTAINS, SpatialOperation.Contains),

    //////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// RCC8 Topological Relations ///////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    // Equals:      TFFFTFFFT
    // Rcc8eq:      TFFFTFFFT
	RCC8_EQ(Rcc8eq.class, GEO_RCC8_EQ, SpatialOperation.IsEqualTo),

    // Disjoint:    FF*FF****
    // Rcc8dc:      FFtFFtttt
	RCC8_DC(Rcc8dc.class, GEO_RCC8_DC, SpatialOperation.IsDisjointTo),

    // Intersects:  ****T****
    // Rcc8ec:      fftfTtttt
	RCC8_EC(Rcc8ec.class, GEO_RCC8_EC, SpatialOperation.Intersects),

    // Overlaps:    T*T***T**
    // Rcc8po:      TtTtttTtt
	RCC8_PO(Rcc8po.class, GEO_RCC8_PO, SpatialOperation.Overlaps),

    // Contains:    T*****FF*
    // Rcc8tppi:    TttfttFFt
	RCC8_TPPI(Rcc8tppi.class, GEO_RCC8_TPPI, SpatialOperation.Contains),

    // Within:      T*F**F***
    // Rcc8tpp:     TfFttFttt
	RCC8_TPP(Rcc8tpp.class, GEO_RCC8_TPP, SpatialOperation.IsWithin),

    // Within:      T*F**F***
    // Rcc8ntpp:    TfFtfFttt
	RCC8_NTPP(Rcc8ntpp.class, GEO_RCC8_NTPP, SpatialOperation.IsWithin),

    // Contains:    T*****FF*
    // Rcc8ntppi:   TttfftFFt
	RCC8_NTPPI(Rcc8ntppi.class, GEO_RCC8_NTPPI, SpatialOperation.Contains),

	;

	private final static Map<SpatialOperation, SpatialOperation> INVERSE_SPATIAL_OPERATIONS;
	static {
		INVERSE_SPATIAL_OPERATIONS = new HashMap<>();
		INVERSE_SPATIAL_OPERATIONS.put(SpatialOperation.IsEqualTo, SpatialOperation.IsEqualTo);
		INVERSE_SPATIAL_OPERATIONS.put(SpatialOperation.IsDisjointTo, SpatialOperation.IsDisjointTo);
		INVERSE_SPATIAL_OPERATIONS.put(SpatialOperation.Intersects, SpatialOperation.Intersects);
		INVERSE_SPATIAL_OPERATIONS.put(SpatialOperation.IsWithin, SpatialOperation.Contains);
		INVERSE_SPATIAL_OPERATIONS.put(SpatialOperation.Contains, SpatialOperation.IsWithin);
		INVERSE_SPATIAL_OPERATIONS.put(SpatialOperation.Overlaps, SpatialOperation.Overlaps);
	}

	private final AbstractBooleanBinaryFunction functionImplementation;
	private final IRI predicateUri;
	private SpatialOperation spatialOperation;

	GeoSparqlFunction(Class<? extends AbstractBooleanBinaryFunction> functionClass, IRI predicateUri, SpatialOperation spatialOperation) {
		try {
			this.functionImplementation = functionClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		this.predicateUri = predicateUri;
		this.spatialOperation = spatialOperation;
	}

	public IRI getPredicateUri() {
		return predicateUri;
	}

	public SpatialOperation getSpatialOperation() {
		return spatialOperation;
	}

	public SpatialOperation getInverseSpatialOperation() {
		return INVERSE_SPATIAL_OPERATIONS.get(spatialOperation);
	}

	public boolean evaluate(Geometry argument1, Geometry argument2) {
		return functionImplementation.evaluate(argument1, argument2);
	}

}