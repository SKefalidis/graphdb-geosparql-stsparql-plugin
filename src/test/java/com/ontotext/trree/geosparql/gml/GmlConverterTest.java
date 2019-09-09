package com.ontotext.trree.geosparql.gml;

import org.locationtech.jts.geom.*;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Tsvetan Dimitrov <tsvetan.dimitrov@ontotext.com>
 * @since 14 Sep 2015.
 */
public class GmlConverterTest {

    private Geometry gml2Geometry(String gmlFileName) throws IOException {
        String gmlString = IOUtils.toString(
                new FileInputStream("src/test/resources/gml/" + gmlFileName));
        GmlConverter gmlConverter;
        try {
            gmlConverter = new GmlConverter();
            return gmlConverter.gmlToGeometry(gmlString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Cannot unmarshall gml!");
        }

        return null;
    }

    @Test
    public void testGML2GeometryForPoint() throws IOException {
        final Geometry point1 = gml2Geometry("point1.gml");
        final Geometry point2 = gml2Geometry("point2.gml");
        assertNotNull(point1);
        assertNotNull(point2);

        assertEquals(point1.getGeometryType(), "Point");
        assertEquals(point2.getGeometryType(), "Point");

        assertEquals(point1.getCoordinate().x, 0.0, 0.0);
        assertEquals(point1.getCoordinate().y, 1.0, 0.0);
        assertEquals(point2.getCoordinate().x, 5.0, 0.0);
        assertEquals(point2.getCoordinate().y, 3.0, 0.0);

        assertEquals(point1.difference(point2), point1);
    }

    @Test
    public void testGML2GeometryForMultiPoint() throws IOException {
        final Geometry geometry = gml2Geometry("multipoint.gml");
        assertNotNull(geometry);
        assertEquals(geometry.getGeometryType(), "MultiPoint");
        assertEquals(geometry.getArea(), 0.0, 0);
    }

    @Test
    public void testGML2GeometryForLineString() throws IOException {
        final Geometry lineString1 = gml2Geometry("linestring1.gml");
        final Geometry lineString2 = gml2Geometry("linestring2.gml");

        assertNotNull(lineString1);
        assertNotNull(lineString2);

        assertEquals(lineString1.getGeometryType(), "LineString");
        assertFalse(lineString1.crosses(lineString2));

        assertArrayEquals(lineString1.getCoordinates(), new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)});

        assertArrayEquals(lineString2.getCoordinates(), new Coordinate[]{
                new Coordinate(2, 1),
                new Coordinate(0, 1),
                new Coordinate(1, 0),
                new Coordinate(3, 4)});
    }

    @Test
    public void testGML2GeometryForMultiLineString() throws IOException {
        final Geometry geometry = gml2Geometry("multilinestring.gml");
        assertNotNull(geometry);
        assertEquals(geometry.getGeometryType(), "MultiLineString");
        assertEquals(geometry.getArea(), 0.0, 0);
    }

    @Test
    public void testGML2GeometryForPolygon() throws IOException {
        final Geometry polygon = gml2Geometry("polygon.gml");
        assertNotNull(polygon);
        assertEquals(polygon.getGeometryType(), "Polygon");
        assertEquals(polygon.getArea(), 1.0, 0);

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        final Polygon convexHullPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)});

        assertEquals(polygon.convexHull(), convexHullPolygon);
    }

    @Test
    public void testGML2GeometryForMultiPolygon() throws IOException {
        final Geometry multiPolygon = gml2Geometry("multipolygon.gml");
        assertNotNull(multiPolygon);
        assertEquals(multiPolygon.getGeometryType(), "MultiPolygon");
        assertEquals(multiPolygon.getArea(), 1.0, 0);
    }
}
