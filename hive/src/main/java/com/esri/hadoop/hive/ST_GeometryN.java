package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCMultiLineString;
import com.esri.core.geometry.ogc.OGCMultiPoint;
import com.esri.core.geometry.ogc.OGCMultiPolygon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;

@Description(
        name = "ST_GeometryN",
        value = "_FUNC_(ST_GeometryCollection, n) - return the nth ST_Geometry in the collection (1-based index)",
        extended = "Example:\n"
                + "  SELECT _FUNC_(ST_GeomFromText('multipoint ((10 40), (40 30), (20 20), (30 10))'), 3) FROM src LIMIT 1;  -- ST_Point(20 20)\n"
                + "  SELECT _FUNC_(ST_GeomFromText('multilinestring ((2 4, 10 10), (20 20, 7 8))'), 2) FROM src LIMIT 1;  -- ST_Linestring(20 20, 7 8)\n"
)

public class ST_GeometryN extends ST_GeometryAccessor {
    static final Log LOG = LogFactory.getLog(ST_GeometryN.class.getName());

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

        int idx = index.get() - 1;  // 1-based UI, 0-based engine
        try {
            GeometryUtils.OGCType ogcType = GeometryUtils.getType(geomref);
            OGCGeometry ogcGeom = null;
            switch (ogcType) {
                case ST_POINT:
                    LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_MULTIPOINT, ogcType);
                    return null;
                case ST_LINESTRING:
                    LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_MULTILINESTRING, ogcType);
                    return null;
                case ST_POLYGON:
                    LogUtils.Log_InvalidType(LOG, GeometryUtils.OGCType.ST_MULTIPOLYGON, ogcType);
                    return null;
                case ST_MULTIPOINT:
                    ogcGeom = ((OGCMultiPoint) ogcGeometry).geometryN(idx);
                    break;
                case ST_MULTILINESTRING:
                    ogcGeom = ((OGCMultiLineString) ogcGeometry).geometryN(idx);
                    break;
                case ST_MULTIPOLYGON:
                    ogcGeom = ((OGCMultiPolygon) ogcGeometry).geometryN(idx);
                    break;
            }
            return GeometryUtils.geometryToEsriShapeBytesWritable(ogcGeom);
        } catch (Exception e) {
            LogUtils.Log_InternalError(LOG, "ST_GeometryN: " + e);
            return null;
        }
    }
}
