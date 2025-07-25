package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;

@Description(name = "ST_Length",
        value = "_FUNC_(line) - returns the length of line",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_Line(0.0,0.0, 3.0,4.0)) FROM src LIMIT 1;  --  5.0"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_Length(ST_SetSRID(ST_LineString(0.0,0.0, 3.0,4.0), 0)) from onerow",
//			result = "5.0"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Length(ST_SetSRID(ST_MultiLineString(array(1,1, 1,2), array(10,10, 20,10)), 0)) from onerow",
//			result = "11"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Length(null) from onerow",
//			result = "null"
//			)
//	}
//)

public class ST_Length extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_Length.class.getName());
    final DoubleWritable resultDouble = new DoubleWritable();

    public DoubleWritable evaluate(BytesWritable geomref) {
        if (geomref == null || geomref.getLength() == 0) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(geomref);
        if (ogcGeometry == null) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        resultDouble.set(ogcGeometry.getEsriGeometry().calculateLength2D());
        return resultDouble;
    }
}
