package com.esri.hadoop.hive;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;

@Description(name = "ST_GeodesicLengthWGS84",
        value = "_FUNC_(line) - returns distance along line on WGS84 spheroid, in meters, for geographic coordinates",
        extended = "Requires the geometry to be in in WGS84 spatial reference, else returns NULL\nExample:\n"
                + " SELECT _FUNC_(ST_SetSRID(ST_Linestring(0.0,0.0, 0.3,0.4), 4326)) FROM src LIMIT 1; -- 55km\n"
                + " SELECT _FUNC_(ST_GeomFromText('MultiLineString((0.0 80.0, 0.3 80.4))', 4326)) FROM src LIMIT 1; -- 45km\n"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select substr(ST_GeodesicLengthWGS84(ST_GeomFromText('LineString(0 0, 0.03 0.04)', 4326)), 1, 5) from onerow",
//			result = "5542."
//			),
//		@HivePdkUnitTest(
//			query = "select substr(ST_GeodesicLengthWGS84(ST_GeomFromText('MultiLineString((0 80, 0.03 80.04))', 4326)), 1, 5) from onerow",
//			result = "4503."
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Length(null) from onerow",
//			result = "null"
//			)
//	}
//)

public class ST_GeodesicLengthWGS84 extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_GeodesicLengthWGS84.class.getName());
    final DoubleWritable resultDouble = new DoubleWritable();

    public DoubleWritable evaluate(BytesWritable geomref) {
        if (geomref == null || geomref.getLength() == 0) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        int WGS84 = 4326;
        if (GeometryUtils.getWKID(geomref) != WGS84) {
            LogUtils.Log_SRIDMismatch(LOG, geomref, WGS84);
            return null;
        }

        OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(geomref);
        if (ogcGeometry == null) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        Geometry esriGeom = ogcGeometry.getEsriGeometry();
        switch (esriGeom.getType()) {
            case Point:
            case MultiPoint:
                resultDouble.set(0.);
                break;
            default:
                MultiPath lines = (MultiPath) (esriGeom);
                int nPath = lines.getPathCount();
                double length = 0.;
                for (int ix = 0; ix < nPath; ix++) {
                    int curPt = lines.getPathStart(ix);
                    int pastPt = lines.getPathEnd(ix);
                    Point fromPt = lines.getPoint(curPt);
                    Point toPt = null;
                    for (int vx = curPt + 1; vx < pastPt; vx++) {
                        toPt = lines.getPoint(vx);
                        length += GeometryEngine.geodesicDistanceOnWGS84(fromPt, toPt);
                        fromPt = toPt;
                    }
                }
                resultDouble.set(length);
                break;
        }

        return resultDouble;
    }
}
