package com.ontotext.trree.geosparql.lucene;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.useekm.types.GeoConvert;
import com.useekm.types.exception.InvalidGeometryException;
import org.locationtech.jts.geom.Geometry;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialArgsParser;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

import java.io.IOException;

/**
 * This class serves as example code to show how to use the Lucene spatial
 * module.
 */
@Ignore
public class LuceneWKTDataTest {
	@Test
	public void test() throws Exception {
		init();
		indexPoints();
		search();
	}

	@Test
	public void testJTS() throws InvalidGeometryException, IOException {
		init();

		Geometry a = GeoConvert.wktToGeometry("Polygon((-83.6 34.1, -83.2 34.1, -83.2 34.5, -83.6 34.5, -83.6 34.1))");
		Geometry b = GeoConvert.wktToGeometry("Polygon((-83.6 34.1, -83.4 34.1, -83.4 34.3, -83.6 34.3, -83.6 34.1))");
		Geometry c = GeoConvert.wktToGeometry("Polygon((-83.2 34.3, -83.0 34.3, -83.0 34.5, -83.2 34.5, -83.2 34.3))");
		Geometry d = GeoConvert.wktToGeometry("Polygon((-83.3 34.0, -83.1 34.0, -83.1 34.2, -83.3 34.2, -83.3 34.0))");
		Geometry e = GeoConvert.wktToGeometry("LineString(-83.4 34.0, -83.3 34.3)");
		Geometry f = GeoConvert.wktToGeometry("Point(-83.4 34.4)");
		Geometry sf = GeoConvert.wktToGeometry("Polygon((23.2 42.6, 23.5 42.6, 23.5 42.8, 23.2 42.8, 23.2 42.6))");
		Geometry bg = GeoConvert.wktToGeometry("Polygon((22 41, 29 41, 29 45, 22 45, 22 41))");

		JtsGeometry sA = new JtsGeometry(a, ctx, true, true);
		JtsGeometry sB = new JtsGeometry(b, ctx, true, true);
		JtsGeometry sC = new JtsGeometry(c, ctx, true, true);
		JtsGeometry sD = new JtsGeometry(d, ctx, true, true);
		JtsGeometry sE = new JtsGeometry(e, ctx, true, true);
		JtsGeometry sF = new JtsGeometry(f, ctx, true, true);
		JtsGeometry sSF = new JtsGeometry(sf, ctx, true, true);
		JtsGeometry sBG = new JtsGeometry(bg, ctx, true, true);

		IndexWriterConfig iwConfig = new IndexWriterConfig();
		IndexWriter indexWriter = new IndexWriter(directory, iwConfig);

		indexWriter.addDocument(newSampleDocument(1, sA));
		indexWriter.addDocument(newSampleDocument(2, sB));
		indexWriter.addDocument(newSampleDocument(3, sC));
		indexWriter.addDocument(newSampleDocument(4, sD));
		indexWriter.addDocument(newSampleDocument(5, sE));
		indexWriter.addDocument(newSampleDocument(6, sF));
		indexWriter.addDocument(newSampleDocument(100, sSF));
		indexWriter.addDocument(newSampleDocument(101, sBG));

		indexWriter.close();


		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

		{
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, sA);
			TopDocs docs = indexSearcher.search(new BooleanQuery.Builder()
					.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
					.add(strategy.makeQuery(args), BooleanClause.Occur.FILTER).build(), 10, idSort);
			for (ScoreDoc doc : docs.scoreDocs) {
				System.out.println("MATCH A: " + indexReader.document(doc.doc).getField("id"));
			}
		}
		{
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, sSF);
			TopDocs docs = indexSearcher.search(new BooleanQuery.Builder()
					.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
					.add(strategy.makeQuery(args), BooleanClause.Occur.FILTER).build(), 10, idSort);
			for (ScoreDoc doc : docs.scoreDocs) {
				System.out.println("MATCH SF: " + indexReader.document(doc.doc).getField("id"));
			}
		}
		{
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
					new JtsGeometry(GeoConvert.wktToGeometry("LineString(23.389 42.696, 23.439 42.693)"), ctx, true, true));
			TopDocs docs = indexSearcher.search(new BooleanQuery.Builder()
					.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
					.add(strategy.makeQuery(args), BooleanClause.Occur.FILTER).build(), 10, idSort);
			for (ScoreDoc doc : docs.scoreDocs) {
				System.out.println("MATCH SOF: " + indexReader.document(doc.doc).getField("id"));
			}
		}

		indexReader.close();




	}

	/**
	 * The Spatial4j {@link SpatialContext} is a sort of global-ish singleton
	 * needed by Lucene spatial.  It's a facade to the rest of Spatial4j, acting
	 * as a factory for {@link Shape}s and provides access to reading and writing
	 * them from Strings.
	 */
	private JtsSpatialContext ctx;//"ctx" is the conventional variable name

	/**
	 * The Lucene spatial {@link SpatialStrategy} encapsulates an approach to
	 * indexing and searching shapes, and providing distance values for them.
	 * It's a simple API to unify different approaches. You might use more than
	 * one strategy for a shape as each strategy has its strengths and weaknesses.
	 * <p />
	 * Note that these are initialized with a field name.
	 */
	private SpatialStrategy strategy;

	private Directory directory;

	protected void init() {
		//Typical geospatial context
		//  These can also be constructed from SpatialContextFactory
		this.ctx = JtsSpatialContext.GEO;

		int maxLevels = 11;//results in sub-meter precision for geohash
		//TODO demo lookup by detail distance
		//  This can also be constructed from SpatialPrefixTreeFactory
		SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

		this.strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");

		this.directory = new RAMDirectory();
	}

	private void indexPoints() throws Exception {
		IndexWriterConfig iwConfig = new IndexWriterConfig();
		IndexWriter indexWriter = new IndexWriter(directory, iwConfig);

		//Spatial4j is x-y order for arguments
		indexWriter.addDocument(newSampleDocument(
				2, ctx.makePoint(-80.93, 33.77)));

		//Spatial4j has a WKT parser which is also "x y" order
		indexWriter.addDocument(newSampleDocument(
				4, ctx.readShapeFromWkt("POINT(60.9289094 -50.7693246)")));

		indexWriter.addDocument(newSampleDocument(
				20, ctx.makePoint(0.1, 0.1), ctx.makePoint(0, 0)));


		indexWriter.close();
	}

	private Document newSampleDocument(int id, Shape... shapes) {
		Document doc = new Document();
		doc.add(new StoredField("id", id));
		doc.add(new NumericDocValuesField("id", id));
		//Potentially more than one shape in this field is supported by some
		// strategies; see the javadocs of the SpatialStrategy impl to see.
		for (Shape shape : shapes) {
			for (Field f : strategy.createIndexableFields(shape)) {
				doc.add(f);
			}
			//store it too; the format is up to you
			//  (assume point in this example)
			//Point pt = (Point) shape;
			//doc.add(new StoredField(strategy.getFieldName(), pt.getX()+" "+pt.getY()));
		}

		return doc;
	}

	private void search() throws Exception {
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

		//--Filter by circle (<= distance from a point)
		{
			//Search with circle
			//note: SpatialArgs can be parsed from a string
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
					ctx.makeCircle(-80.0, 33.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
			TopDocs docs = indexSearcher.search(new BooleanQuery.Builder()
					.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
					.add(strategy.makeQuery(args), BooleanClause.Occur.FILTER).build(), 10, idSort);
			assertDocMatchedIds(indexSearcher, docs, 2);
			//Now, lets get the distance for the 1st doc via computing from stored point value:
			// (this computation is usually not redundant)
			Document doc1 = indexSearcher.doc(docs.scoreDocs[0].doc);
			String doc1Str = doc1.getField(strategy.getFieldName()).stringValue();
			//assume doc1Str is "x y" as written in newSampleDocument()
			int spaceIdx = doc1Str.indexOf(' ');
			double x = Double.parseDouble(doc1Str.substring(0, spaceIdx));
			double y = Double.parseDouble(doc1Str.substring(spaceIdx+1));
			double doc1DistDEG = ctx.calcDistance(args.getShape().getCenter(), x, y);
			Assert.assertEquals(121.6d, DistanceUtils.degrees2Dist(doc1DistDEG, DistanceUtils.EARTH_MEAN_RADIUS_KM),
					0.1);
			//or more simply:
			Assert.assertEquals(121.6d, doc1DistDEG * DistanceUtils.DEG_TO_KM, 0.1);
		}
		//--Match all, order by distance ascending
		{
			Point pt = ctx.makePoint(60, -50);
			DoubleValuesSource valueSource = strategy.makeDistanceValueSource(pt, DistanceUtils.DEG_TO_KM);//the distance (in km)
			Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher);//false=asc dist
			TopDocs docs = indexSearcher.search(new MatchAllDocsQuery(), 10, distSort);
			assertDocMatchedIds(indexSearcher, docs, 4, 20, 2);
			//To get the distance, we could compute from stored values like earlier.
			// However in this example we sorted on it, and the distance will get
			// computed redundantly.  If the distance is only needed for the top-X
			// search results then that's not a big deal. Alternatively, try wrapping
			// the ValueSource with CachingDoubleValueSource then retrieve the value
			// from the ValueSource now. See LUCENE-4541 for an example.
		}
		//demo arg parsing
		{
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
					ctx.makeCircle(-80.0, 33.0, 1));
			SpatialArgs args2 = new SpatialArgsParser().parse("Intersects(BUFFER(POINT(-80 33),1))", ctx);
			Assert.assertEquals(args.toString(), args2.toString());
		}

		indexReader.close();
	}

	private void assertDocMatchedIds(IndexSearcher indexSearcher, TopDocs docs, long... ids) throws IOException {
		long[] gotIds = {docs.totalHits.value};
		for (int i = 0; i < gotIds.length; i++) {
			gotIds[i] = indexSearcher.doc(docs.scoreDocs[i].doc).getField("id").numericValue().intValue();
		}
		Assert.assertArrayEquals(ids, gotIds);
	}

}