package com.ontotext.trree.geosparql;

import com.ontotext.trree.geosparql.lucene.LuceneMultiSearchEntityGeometryIterator;
import com.ontotext.trree.sdk.Entities;
import com.ontotext.trree.sdk.StatementIterator;
import com.useekm.indexing.GeoConstants;
import org.locationtech.jts.geom.Geometry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * The work horse of the implementation. It will use the indexer to get rough results
 * and then invoke the respective GeoSPARQL function to narrow down the exact matches.
 */
class GeoSparqlRelationIterator extends StatementIterator {
	private final GeoSparqlPlugin parent;
	private final Logger logger;
	private final GeoSparqlFunction function;
	private final Entities entities;

	private Geometry knownGeometry;
	private EntityGeometryIterator iKnownEntities;
	private EntityGeometryIterator iCandidateEntities;

	private EntityGeometryIterator iSubjectGeometries;
	private EntityGeometryIterator iObjectGeometries;

	private LuceneMultiSearchEntityGeometryIterator searchIterator;

	private boolean trustLucene;

	private boolean inverse;

	GeoSparqlRelationIterator(GeoSparqlPlugin parent, GeoSparqlFunction function,
	                                 long subject, long predicate, long object, Entities entities) {
		this.parent = parent;
		this.logger = parent.getLogger();
		this.function = function;
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.entities = entities;

		if (subject != 0) {
			// Subject is bound and refers to a Geometry literal or a Geometry/Feature object
			iSubjectGeometries = makeIteratorFromEntityId(subject);
		}

		if (object != 0) {
			// Object is bound and refers to a Geometry literal or a Geometry/Feature object
			iObjectGeometries = makeIteratorFromEntityId(object);
		}

		if (iSubjectGeometries != null && iObjectGeometries != null) {
			// Both subject and object have explicit geometries, don't use Lucene search but match directly
			iKnownEntities = iObjectGeometries;
			iCandidateEntities = iSubjectGeometries;

			inverse = true;
		} else if (iSubjectGeometries == null) {
			// Subject is unknown and candidates will be provided by searching in Lucene with the object
			iKnownEntities = iObjectGeometries;
			iCandidateEntities = searchIterator = new LuceneMultiSearchEntityGeometryIterator(parent.indexer,
					function.getSpatialOperation());

			inverse = true;
		} else {
			// Object is unknown and candidates will be provided by searching in Lucene with the subject
			iKnownEntities = iSubjectGeometries;
			iCandidateEntities = searchIterator = new LuceneMultiSearchEntityGeometryIterator(parent.indexer,
					function.getInverseSpatialOperation());

			inverse = false;
		}
	}

	@Override
	public boolean next() {
		boolean result = false;

		while (true) {
			if (knownGeometry == null || !iCandidateEntities.hasNextGeometry()) {
				if (!iKnownEntities.hasNextGeometry()) {
					// no more known entities, GeoSPARQLRelationIterator ends
					break;
				}

				// Fresh known Geometry. It will be reused until a match is found or no more candidate Geometries left
				knownGeometry = iKnownEntities.nextGeometry();

				if (searchIterator != null) {
					// If we have a search iterator (either subject or object is unbound) we have to notify it
					// about the new known geometry
					searchIterator.search(knownGeometry);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("KNOWN GEOMETRY: {}; {}", entities.get(iKnownEntities.getEntityForLastGeometry()),
							knownGeometry);
				}
			}

			Geometry candidateGeometry = iCandidateEntities.nextGeometry();

			if (candidateGeometry != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("CANDIDATE: {}; {}", entities.get(iCandidateEntities.getEntityForLastGeometry()),
							candidateGeometry);
				}

				result = trustLucene || (inverse ?
						function.evaluate(candidateGeometry, knownGeometry) :
						function.evaluate(knownGeometry, candidateGeometry));
			} else {
				logger.debug(">>>>>>>> GeoSPARQL: No available candidate geometries matching the query!");
				break;
			}

			if (result) {
				// NB: Skips the remaining geometries for this lastEntity as we already found a match
				iKnownEntities.advanceToNextEntity();

				if (logger.isDebugEnabled()) {
					logger.debug("MATCH: {} -> {}", knownGeometry, candidateGeometry);
				}

				if (inverse) {
					subject = iCandidateEntities.getEntityForLastGeometry();
					object = iKnownEntities.getEntityForLastGeometry();
				} else {
					subject = iKnownEntities.getEntityForLastGeometry();
					object = iCandidateEntities.getEntityForLastGeometry();
				}

				// found a pair of subject/object that satisfies the condition
				break;
			}
		}

		return result;
	}


	@Override
	public void close() {
		try {
			iKnownEntities.close();
		} catch (IOException e) {
			logger.warn("Unable to close entity-geometry iterator.", e);
		}

		try {
			iCandidateEntities.close();
		} catch (IOException e) {
			logger.warn("Unable to close entity-geometry iterator.", e);
		}
	}

	/**
	 * Makes an EntityGeometryIterator from a GraphDB entity id.
	 * The id may refer to either a Geometry literal (WKT/GML) or an IRI that describes
	 * a Geometry or Feature object.
	 *
	 * @param entityId an entity id
	 * @return an EntityGeometryIterator
	 */
	private EntityGeometryIterator makeIteratorFromEntityId(long entityId) {
		EntityGeometryIterator iterator;
		Value value = entities.get(entityId);
		if (value instanceof Literal) {
			// the id refers to a literal, we need to parse the geometry
			IRI subjType = ((Literal)value).getDatatype();
			Geometry g;
			if (GeoConstants.GEO_GML_LITERAL.equals(subjType)) {
				// gml
				g = parent.getGeometryFromString(value.stringValue(), parent.asGML);
			} else {
				// wkt
				g = parent.getGeometryFromString(value.stringValue(), parent.asWKT);
			}
			iterator = new SingleEntityGeometryIterator(entityId, g);
		} else {
			// the id refers to an IRI referring to a geometry/feature
			iterator = parent.indexer.getGeometriesFor(entityId);
		}

		return iterator;
	}
}
