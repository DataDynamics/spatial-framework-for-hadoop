package com.esri.hadoop.hive;

import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.io.BytesWritable;

import java.nio.ByteBuffer;

@Description(
        name = "ST_MLineFromWKB",
        value = "_FUNC_(wkb) - construct an ST_MultiLineString from OGC well-known binary",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_AsBinary(ST_GeomFromText('multilinestring ((1 0, 2 3), (5 7, 7 5))'))) FROM src LIMIT 1;  -- constructs ST_MultiLineString\n"
)
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_GeometryType(ST_MLineFromWKB(ST_AsBinary(ST_GeomFromText('multilinestring ((1 2, 2 1),(10 10, 20 20))')))) from onerow",
//			result = "ST_MULTILINESTRING"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Equals(ST_MLineFromWKB(ST_AsBinary(ST_GeomFromText('multilinestring ((1 2, 2 1),(10 10, 20 20))'))), ST_GeomFromText('multilinestring ((1 2, 2 1),(10 10, 20 20))')) from onerow",
//			result = "true"
//			)
//		}
//	)

public class ST_MLineFromWKB extends ST_Geometry {

    static final Log LOG = LogFactory.getLog(ST_MLineFromWKB.class.getName());

    public BytesWritable evaluate(BytesWritable wkb) throws UDFArgumentException {
        return evaluate(wkb, 0);
    }

    public BytesWritable evaluate(BytesWritable wkb, int wkid) throws UDFArgumentException {

        try {
            SpatialReference spatialReference = null;
            if (wkid != GeometryUtils.WKID_UNKNOWN) {
                spatialReference = SpatialReference.create(wkid);
            }
            byte[] byteArr = wkb.getBytes();
            ByteBuffer byteBuf = ByteBuffer.allocate(byteArr.length);
            byteBuf.put(byteArr);
            OGCGeometry ogcObj = OGCGeometry.fromBinary(byteBuf);
            ogcObj.setSpatialReference(spatialReference);
            String gType = ogcObj.geometryType();
            if (gType.equals("MultiLineString") || gType.equals("LineString")) {
                return GeometryUtils.geometryToEsriShapeBytesWritable(ogcObj);
            } else {
                LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_MULTILINESTRING, GeometryUtils.OGCType.UNKNOWN);
                return null;
            }
        } catch (Exception e) {  // IllegalArgumentException, GeometryException
            LOG.error(e.getMessage());
            return null;
        }
    }

}
