package com.esri.hadoop.hive;

import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

import java.util.List;

@Description(
        name = "ST_MultiLineString",
        value = "_FUNC_(array(x1, y1, x2, y2, ... ), array(x1, y1, x2, y2, ... ), ... ) - constructor for 2D multi line string\n"
                + "_FUNC_('multilinestring( ... )') - constructor for 2D multi line string",
        extended = "Example:\n" +
                "  SELECT _FUNC_(array(1, 1, 2, 2), array(10, 10, 20, 20)) from src LIMIT 1;\n" +
                "  SELECT _FUNC_('multilinestring ((1 1, 2 2), (10 10, 20 20))', 0) from src LIMIT 1;")
//@HivePdkUnitTests(
//	cases = {
//		@HivePdkUnitTest(
//			query = "select st_asjson(ST_MultiLineString(1, 1, 2, 2, 3, 3)) from onerow",
//			result = "{\"points\":[[1.0,1.0],[2.0,2.0],[3.0,3.0]]}"
//			),
//		@HivePdkUnitTest(
//			query = "select ST_Equals(ST_MultiLinestring('multilinestring ((2 4, 10 10), (20 20, 7 8))'), ST_GeomFromText('multilinestring ((2 4, 10 10), (20 20, 7 8))')) from onerow",
//			result = "true"
//			)
//		}
//)
public class ST_MultiLineString extends ST_Geometry {

    static final Log LOG = LogFactory.getLog(ST_MultiLineString.class.getName());

    // Number-pairs constructor
    public BytesWritable evaluate(List<DoubleWritable>... multipaths) throws UDFArgumentLengthException {

        if (multipaths == null || multipaths.length == 0) {
            LogUtils.Log_ArgumentsNull(LOG);
            return null;
        }

        try {
            Polyline mPolyline = new Polyline();

            int arg_idx = 0;
            for (List<DoubleWritable> multipath : multipaths) {
                if (multipath.size() % 2 != 0) {
                    LogUtils.Log_VariableArgumentLengthXY(LOG, arg_idx);
                    return null;
                }

                mPolyline.startPath(multipath.get(0).get(), multipath.get(1).get());

                for (int i = 2; i < multipath.size(); i += 2) {
                    mPolyline.lineTo(multipath.get(i).get(), multipath.get(i + 1).get());
                }
                arg_idx++;
            }

            return GeometryUtils.geometryToEsriShapeBytesWritable(OGCGeometry.createFromEsriGeometry(mPolyline, null, true));
        } catch (Exception e) {
            LogUtils.Log_InternalError(LOG, "ST_MultiLineString: " + e);
            return null;
        }
    }

    // WKT constructor  -  can use SetSRID on constructed multi-linestring
    public BytesWritable evaluate(Text wkwrap) throws UDFArgumentException {
        String wkt = wkwrap.toString();
        try {
            OGCGeometry ogcObj = OGCGeometry.fromText(wkt);
            ogcObj.setSpatialReference(null);
            if (ogcObj.geometryType().equals("MultiLineString")) {
                return GeometryUtils.geometryToEsriShapeBytesWritable(ogcObj);
            } else {
                LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_MULTILINESTRING, GeometryUtils.OGCType.UNKNOWN);
                return null;
            }

        } catch (Exception e) {  // IllegalArgumentException, GeometryException
            LogUtils.Log_InvalidText(LOG, wkt);
            return null;
        }
    }

}
