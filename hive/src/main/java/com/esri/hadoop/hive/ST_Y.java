package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;

@Description(name = "ST_Y",
        value = "_FUNC_(point) - returns the Y coordinate of point",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_Point(1.5, 2.5)) FROM src LIMIT 1;  --  2.5"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_Y(ST_Point(1,2)) from onerow",
//			result = "2"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Y(ST_LineString(1.5,2.5, 3.0,2.2)) from onerow",
//			result = "null"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Y(null) from onerow",
//			result = "null"
//			)
//	}
//)

public class ST_Y extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_Y.class.getName());
    final DoubleWritable resultDouble = new DoubleWritable();

    public DoubleWritable evaluate(BytesWritable geomref) {
        if (geomref == null || geomref.getLength() == 0) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(geomref);
        if (ogcGeometry == null) {
            return null;
        }

        switch (GeometryUtils.getType(geomref)) {
            case ST_POINT:
                OGCPoint pt = (OGCPoint) ogcGeometry;
                resultDouble.set(pt.Y());
                return resultDouble;
            default:
                LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_POINT, GeometryUtils.getType(geomref));
                return null;
        }
    }

}
