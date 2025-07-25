package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCMultiLineString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;

@Description(
        name = "ST_Boundary",
        value = "_FUNC_(ST_Geometry) - boundary of the input ST_Geometry",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_LineString(0,1, 1,0))) FROM src LIMIT 1;   -- MULTIPOINT((1 0),(0 1))\n"
                + "  SELECT _FUNC_(ST_Polygon(1,1, 4,1, 1,4)) FROM src LIMIT 1;  -- LINESTRING(1 1, 4 1, 1 4, 1 1)\n"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_GeometryType(ST_Boundary(ST_Linestring('linestring (10 10, 20 20)'))) from onerow",
//			result = "ST_MULTIPOINT"
//			 ),
//		@HivePdkUnitTest(
//			query = "select ST_Equals(ST_Boundary(ST_Linestring('linestring (10 10, 20 20)')), ST_GeomFromText('multipoint ((10 10), (20 20))')) from onerow",
//			result = "true"
//			)
//		}
//	)

// The boundary of a point (or multipoint) is the empty set  OGC 4.18, 6.1.5
// The boundary of a closed curve is empty; non-closed curve, its 2 end points  OGC 6.1.6.1
// The boundary of a surface is the set of closed curves that form its limits  OGC 4.21

public class ST_Boundary extends ST_GeometryProcessing {
    static final Log LOG = LogFactory.getLog(ST_Boundary.class.getName());

    public BytesWritable evaluate(BytesWritable geomref) {
        if (geomref == null || geomref.getLength() == 0) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(geomref);
        if (ogcGeometry == null) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }
        try {
            OGCGeometry boundGeom = ogcGeometry.boundary();
            if (boundGeom.geometryType().equals("MultiLineString") && ((OGCMultiLineString) boundGeom).numGeometries() == 1)
                boundGeom = ((OGCMultiLineString) boundGeom).geometryN(0);  // match ST_Boundary/SQL-RDBMS
            return GeometryUtils.geometryToEsriShapeBytesWritable(boundGeom);
        } catch (Exception e) {
            LogUtils.Log_InternalError(LOG, "ST_Boundary: " + e);
            return null;
        }
    }

}
