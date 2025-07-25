package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;

@Description(name = "ST_MinM",
        value = "_FUNC_(geometry) - returns the minimum M coordinate of geometry",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_PointM(1.5, 2.5, 2)) FROM src LIMIT 1;  -- 2\n"
                + "  SELECT _FUNC_(ST_LineString('linestring m (1.5 2.5 2, 3.0 2.2 1)')) FROM src LIMIT 1;  -- 1\n"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_MinM(ST_PointM(0., 3., 1.)) from onerow",
//			result = "1.0"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_MinM(ST_GeomFromText('linestring m (10 10 2, 20 20 4)')) from onerow",
//			result = "2.0"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_MinM(ST_MultiPoint('multipoint m((0 0 1), (2 2 3)')) from onerow",
//			result = "1.0"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_MinM(ST_Point(1,2)) from onerow",
//			result = "null"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_MinM(null) from onerow",
//			result = "null"
//			)
//	}
//)

public class ST_MinM extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_MinM.class.getName());
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
        if (!ogcGeometry.isMeasured()) {
            LogUtils.Log_NotMeasured(LOG);
            return null;
        }

        resultDouble.set(ogcGeometry.MinMeasure());
        return resultDouble;
    }

}
