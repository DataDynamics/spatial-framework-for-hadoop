package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;


@Description(
        name = "ST_Polygon",
        value = "_FUNC_(x, y, [x, y]*) - constructor for 2D polygon\n" +
                "_FUNC_('polygon( ... )') - constructor for 2D polygon",
        extended = "Example:\n" +
                "  SELECT _FUNC_(1, 1, 1, 4, 4, 4, 4, 1) from src LIMIT 1;  -- creates a rectangle\n" +
                "  SELECT _FUNC_('polygon ((1 1, 4 1, 1 4))') from src LIMIT 1;  -- creates a triangle")
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select ST_GeometryType(ST_Polygon('polygon ((0 0, 10 0, 0 10, 0 0))')) from onerow",
//			result = "ST_POLYGON"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_GeometryType(ST_Polygon('polygon ((0 0, 8 0, 0 8, 0 0), (1 1, 1 5, 5 1, 1 1))')) from onerow",
//			result = "ST_POLYGON"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Equals(ST_Polygon('polygon ((0 0, 10 0, 0 10, 0 0))'), ST_GeomFromText('polygon ((0 0, 10 0, 0 10, 0 0))')) from onerow",
//			result = "true"
//			)
//		}
//	)

public class ST_Polygon extends ST_Geometry {

    static final Log LOG = LogFactory.getLog(ST_Polygon.class.getName());

    // Number-pairs constructor
    public BytesWritable evaluate(DoubleWritable... xyPairs) throws UDFArgumentLengthException {

        if (xyPairs == null || xyPairs.length < 6 || xyPairs.length % 2 != 0) {
            LogUtils.Log_VariableArgumentLengthXY(LOG);
            return null;
        }

        try {
            double xStart = xyPairs[0].get(), yStart = xyPairs[1].get();
            String wkt = "polygon((" + xStart + " " + yStart;

            int i; // index persists after first loop
            for (i = 2; i < xyPairs.length; i += 2) {
                wkt += ", " + xyPairs[i] + " " + xyPairs[i + 1];
            }
            double xEnd = xyPairs[i - 2].get(), yEnd = xyPairs[i - 1].get();
            // This counts on the same string getting parsed to double exactly equally
            if (xEnd != xStart || yEnd != yStart)
                wkt += ", " + xStart + " " + yStart;  // close the ring

            wkt += "))";

            return evaluate(new Text(wkt));
        } catch (Exception e) {
            LogUtils.Log_InternalError(LOG, "ST_Polygon: " + e);
            return null;
        }
    }

    // WKT constructor - can use SetSRID on constructed polygon
    public BytesWritable evaluate(Text wkwrap) throws UDFArgumentException {
        String wkt = wkwrap.toString();
        try {
            OGCGeometry ogcObj = OGCGeometry.fromText(wkt);
            ogcObj.setSpatialReference(null);
            if (ogcObj.geometryType().equals("Polygon")) {
                return GeometryUtils.geometryToEsriShapeBytesWritable(ogcObj);
            } else {
                LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_POLYGON, GeometryUtils.OGCType.UNKNOWN);
                return null;
            }
        } catch (Exception e) {  // IllegalArgumentException, GeometryException
            LogUtils.Log_InvalidText(LOG, wkt);
            return null;
        }
    }
}
