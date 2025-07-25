package com.esri.hadoop.hive;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

import com.esri.core.geometry.Point;


public class TestStCentroid {

	private final static double Epsilon = 0.0001;

	@Test
	public void TestSimplePointCentroid() throws Exception {
		final ST_Centroid stCtr = new ST_Centroid();
		final ST_Point stPt = new ST_Point();
		BytesWritable bwGeom = stPt.evaluate(new Text("point (2 3)"));
		BytesWritable bwCentroid = stCtr.evaluate(bwGeom);
		validatePoint(new Point(2,3), bwCentroid);
	}

	@Test
	public void TestMultiPointCentroid() throws Exception {
		final ST_Centroid stCtr = new ST_Centroid();
		final ST_MultiPoint stMp = new ST_MultiPoint();
		BytesWritable bwGeom = stMp.evaluate(new Text("multipoint ((0 0), (1 1), (1 -1), (6 0))"));
		BytesWritable bwCentroid = stCtr.evaluate(bwGeom);
		validatePoint(new Point(2,0), bwCentroid);
	}

	@Test
	public void TestLineCentroid() throws Exception {
		final ST_Centroid stCtr = new ST_Centroid();
		final ST_LineString stLn = new ST_LineString();
		BytesWritable bwGeom = stLn.evaluate(new Text("linestring (0 0, 6 0)"));
		BytesWritable bwCentroid = stCtr.evaluate(bwGeom);
		validatePoint(new Point(3,0), bwCentroid);
		bwGeom = stLn.evaluate(new Text("linestring (0 0, 0 4, 12 4)"));
		bwCentroid = stCtr.evaluate(bwGeom);
		// L1 = 4, L2 = 12, W1 = 0.25, W2 = 0.75, X = W1 * 0 + W2 * 6, Y = W1 * 2 + W2 * 4
		// Or like centroid of multipoint of 1 of (0 2) and 3 of (6 4)
		validatePoint(new Point(4.5, 3.5), bwCentroid);
	}

	@Test
	public void TestPolygonCentroid() throws Exception {
		final ST_Centroid stCtr = new ST_Centroid();
		final ST_Polygon stPoly = new ST_Polygon();
		BytesWritable bwGeom = stPoly.evaluate(new Text("polygon ((0 0, 0 8, 8 8, 8 0, 0 0))"));
		BytesWritable bwCentroid = stCtr.evaluate(bwGeom);
		validatePoint(new Point(4,4), bwCentroid);
		bwGeom = stPoly.evaluate(new Text("polygon ((1 1, 5 1, 3 4, 1 1))"));
		bwCentroid = stCtr.evaluate(bwGeom);
		validatePoint(new Point(3,2), bwCentroid);
		bwGeom = stPoly.evaluate(new Text("polygon ((14 0, -14 0, -2 24, 2 24, 14 0))"));
		bwCentroid = stCtr.evaluate(bwGeom);        // Cross-checked with ...
		validatePoint(new Point(0,9), bwCentroid);  // ... omnicalculator.com/math/centroid
	}

	/**
	 * Validates the centroid geometry writable.
	 * 
	 * @param point
	 *            the represented point location.
	 * @param geometryAsWritable
	 *            the geometry represented as {@link BytesWritable}.
	 */
	private static void validatePoint(Point point, BytesWritable geometryAsWritable) {
		ST_X getX = new ST_X();
		ST_Y getY = new ST_Y();
		DoubleWritable xAsWritable = getX.evaluate(geometryAsWritable);
		DoubleWritable yAsWritable = getY.evaluate(geometryAsWritable);

		if (null == xAsWritable || null == yAsWritable ||
			Math.abs(point.getX() - xAsWritable.get()) > Epsilon ||
			Math.abs(point.getY() - yAsWritable.get()) > Epsilon)
			System.err.println("validateCentroid: " + (new ST_AsText()).evaluate(geometryAsWritable)
							   + " ~ " + point);

		assertNotNull("The x writable must not be null!", xAsWritable);
		assertNotNull("The y writable must not be null!", yAsWritable);
		assertEquals("Longitude is different!", point.getX(), xAsWritable.get(), Epsilon);
		assertEquals("Latitude is different!", point.getY(), yAsWritable.get(), Epsilon);
	}

}
