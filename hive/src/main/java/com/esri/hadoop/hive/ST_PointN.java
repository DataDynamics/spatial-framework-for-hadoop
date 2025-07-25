package com.esri.hadoop.hive;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;

@Description(
        name = "ST_PointN",
        value = "_FUNC_(ST_Geometry, n) - returns the point that is the nth vertex in an ST_Linestring or ST_MultiPoint (1-based index)",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_LineString(1.5,2.5, 3.0,2.2), 2) FROM src LIMIT 1;  -- POINT(3.0 2.2)\n"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_X(ST_PointN(ST_GeomFromText('multipoint ((10 40), (40 30), (20 20), (30 10))', 0), 2)) from onerow",
//			result = "40"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Y(ST_PointN(ST_GeomFromText('multipoint ((10 40), (40 30), (20 20), (30 10))', 0), 2)) from onerow",
//			result = "30"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_X(ST_PointN(ST_GeomFromtext('linestring (10.02 20.01, 10.32 23.98, 11.92 25.64)'), 1)) from onerow",
//			result = "10.02"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Y(ST_PointN(ST_GeomFromtext('linestring (10.02 20.01, 10.32 23.98, 11.92 25.64)'), 1)) from onerow",
//			result = "20.01"
//			),
//		@HivePdkUnitTest(
//			query = "ST_PointN(ST_GeomFromText('multipoint ((10 40), (40 30), (20 20), (30 10))', 0), 5) from onerow",
//			result = "null"
//			)
//		}
//	)

public class ST_PointN extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_PointN.class.getName());

    public BytesWritable evaluate(BytesWritable geomref, IntWritable index) {
        if (geomref == null || geomref.getLength() == 0 || index == null) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(geomref);
        if (ogcGeometry == null) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        Geometry esriGeom = ogcGeometry.getEsriGeometry();
        Point pn = null;
        int idx = index.get();
        idx = (idx == 0) ? 0 : idx - 1;  // consistency with SDE ST_Geometry
        switch (esriGeom.getType()) {
            case Line:
            case Polyline:
                MultiPath lines = (MultiPath) (esriGeom);
                try {
                    pn = lines.getPoint(idx);
                } catch (Exception e) {
                    LogUtils.Log_InvalidIndex(LOG, idx + 1, 1, lines.getPointCount());
                    return null;
                }
                break;
            case MultiPoint:
                MultiPoint mp = (MultiPoint) (esriGeom);
                try {
                    pn = mp.getPoint(idx);
                } catch (Exception e) {
                    LogUtils.Log_InvalidIndex(LOG, idx + 1, 1, mp.getPointCount());
                    return null;
                }
                break;
            default:  // ST_Geometry ST_PointN gives ERROR on Point or Polygon (on PostgreSQL)
                LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_LINESTRING, GeometryUtils.getType(geomref));
                return null;
        }
        return GeometryUtils.geometryToEsriShapeBytesWritable(pn,
                GeometryUtils.getWKID(geomref),
                GeometryUtils.OGCType.ST_POINT);
    }
}
