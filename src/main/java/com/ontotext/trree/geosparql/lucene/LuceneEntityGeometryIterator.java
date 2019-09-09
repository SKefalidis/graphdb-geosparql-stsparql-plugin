package com.ontotext.trree.geosparql.lucene;

import com.ontotext.trree.geosparql.EntityGeometryIterator;
import org.locationtech.jts.geom.Geometry;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * An EntityGeometryIterator implementation that returns all Geometries matching a Lucene query.
 */
class LuceneEntityGeometryIterator implements EntityGeometryIterator {
	private final static int PAGE_SIZE = 1000;

	private int page;
	private TopDocs topDocs;
	private final IndexSearcher searcher;
	private Query query;

	private int idx = -1;
	private int geoDatasIndex = 0;
	private BytesRef[] geoDatas;

	private long entityId;
	private Geometry geometry;

	LuceneEntityGeometryIterator(IndexSearcher searcher, Query query) throws IOException {
		this.searcher = searcher;
		this.query = query;

		// Get initial page
		// Note that PAGE_SIZE is currently 1000 and Lucene will track total hits up to 1000+1 by default so it's ok
		// to just use the searcher like this. If page size goes > 1000 or Lucene's total hits tracking default changes
		// we'll need to use a TopDocsCollector.
		topDocs = searcher.search(query, PAGE_SIZE);
		page = 1;
	}

	@Override
	public long getEntityForLastGeometry() {
		return entityId;
	}

	@Override
	public Geometry nextGeometry() {
		if (!hasNextGeometry()) {
			return null;
		}

		try {
			if (geoDatas != null && geoDatasIndex + 1 < geoDatas.length) {
				// next geometry
				geoDatasIndex++;
			} else if (idx + 1 < topDocs.scoreDocs.length) {
				// next doc
				advanceLuceneDocument();
			} else {
				// next page
				advanceLucenePage();
			}

			return geometry = LuceneGeoIndexer.fieldValueToGeometry(geoDatas[geoDatasIndex].bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Geometry lastGeometry() {
		return geometry;
	}

	@Override
	public boolean hasNextGeometry() {
		return  // There are more geometries in this document
				(geoDatas != null && geoDatasIndex + 1 < geoDatas.length)
				// There are more documents in this page
				|| idx + 1 < topDocs.scoreDocs.length
				// Lucene has more pages
				|| page * PAGE_SIZE < topDocs.totalHits.value;
	}

	@Override
	public void advanceToNextEntity() {
		geoDatas = null;
	}

	@Override
	public void close() throws IOException {
		searcher.getIndexReader().close();
	}

	private boolean advanceLucenePage() throws IOException {
		if (page * PAGE_SIZE < topDocs.totalHits.value) {
			// Still more results to collect
			topDocs = searcher.searchAfter(topDocs.scoreDocs[topDocs.scoreDocs.length - 1], query, PAGE_SIZE);
			page++;
			idx = -1;
			advanceLuceneDocument();
			return true;
		}
		return false;
	}

	private void advanceLuceneDocument() throws IOException {
		idx++;
		geoDatasIndex = 0;
		ScoreDoc d = topDocs.scoreDocs[idx];
		Document doc = searcher.doc(d.doc);

		entityId = (Long) doc.getField("id").numericValue();
		geoDatas = doc.getBinaryValues("geoData");
	}
}
