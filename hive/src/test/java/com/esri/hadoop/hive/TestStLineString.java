package com.esri.hadoop.hive;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

// select ST_GeometryType(ST_Linestring(10,10, 20,20)) from onerow;
// select ST_GeometryType(ST_Linestring('linestring (10 10, 20 20)')) from onerow;
// select ST_GeometryType(ST_Linestring('linestring z (10 10 2, 20 20 4)')) from onerow;

public class TestStLineString {

	@Test
	public void test() throws Exception {
		ST_GeometryType typer = new ST_GeometryType();
		ST_LineString stLn = new ST_LineString();
		//ST_Equals stEq = new ST_Equals();
		DoubleWritable ten = new DoubleWritable(10);
		DoubleWritable twenty = new DoubleWritable(20);
		BytesWritable bwGeom = stLn.evaluate(ten,ten, twenty);
		assertEquals(null, bwGeom);  // odd arguments
		bwGeom = stLn.evaluate(ten,ten, twenty,twenty);
		Text gty = typer.evaluate(bwGeom);
		assertEquals("ST_LINESTRING", gty.toString());
		Text wkt = new Text("linestring (10 10, 20 20)");
		bwGeom = stLn.evaluate(wkt);
		gty = typer.evaluate(bwGeom);
		assertEquals("ST_LINESTRING", gty.toString());
		//GUDF assertTrue(stEq.~eval~(new ST_GeomFromText().evaluate(wkt), bwGeom));
		bwGeom = stLn.evaluate(new Text("linestring z (10 10 2, 20 20 4)"));
		gty = typer.evaluate(bwGeom);
		assertEquals("ST_LINESTRING", gty.toString());
        ArrayList<DoubleWritable> xs = new ArrayList<DoubleWritable>(Arrays.asList(ten,twenty));
        ArrayList<DoubleWritable> ys = new ArrayList<DoubleWritable>(Arrays.asList(twenty,ten));
		bwGeom = stLn.evaluate(xs, ys);
		gty = typer.evaluate(bwGeom);
		assertEquals("ST_LINESTRING", gty.toString());
		BytesWritable pt1020 = new ST_Point().evaluate(ten,twenty);
		BytesWritable pt2010 = new ST_Point().evaluate(twenty,ten);
		ArrayList<BytesWritable> pts = new ArrayList<BytesWritable>(Arrays.asList(pt1020, pt2010));
		bwGeom = stLn.evaluate(pts);
		gty = typer.evaluate(bwGeom);
		assertEquals("ST_LINESTRING", gty.toString());
	}

}

