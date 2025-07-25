package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;

@Description(
        name = "ST_IsSimple",
        value = "_FUNC_(geometry) - return true if geometry is simple",
        extended = "Example:\n"
                + "  > SELECT _FUNC_(ST_Point(1.5, 2.5)) FROM src LIMIT 1; -- true\n"
                + "  > SELECT _FUNC_(ST_LineString(0.,0., 1.,1., 0.,1., 1.,0.)) FROM src LIMIT 1; -- false\n"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_IsSimple(ST_Point(0,0)) from onerow",
//			result = "true"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_IsSimple(ST_MultiPoint(0,0, 2,2)) from onerow",
//			result = "true"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_IsSimple(ST_LineString(0.,0., 1.,1., 0.,1., 1.,0.)) from onerow",
//			result = "false"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_IsSimple(ST_LineString(0,0, 1,0, 1,1, 0,2, 2,2, 1,1, 2,0)) from onerow",
//			result = "false"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_IsSimple(null) from onerow",
//			result = "null"
//			)
//		}
//	)

public class ST_IsSimple extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_IsSimple.class.getName());
    final BooleanWritable resultBoolean = new BooleanWritable();

    public BooleanWritable evaluate(BytesWritable geomref) {
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
            resultBoolean.set(ogcGeometry.isSimple());
        } catch (Exception e) {
            LogUtils.Log_InternalError(LOG, "ST_IsSimple" + e);
            return null;
        }
        return resultBoolean;
    }

}
