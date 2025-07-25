package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;

@Description(
        name = "ST_GeomFromGeoJSON",
        value = "_FUNC_(json) - construct an ST_Geometry from GeoJSON",
        extended = "Example:\n"
                + "  SELECT _FUNC_('{\"type\":\"Point\", \"coordinates\":[1.2, 2.4]}') FROM src LIMIT 1;  -- constructs ST_Point\n"
                + "  SELECT _FUNC_('{\"type\":\"LineString\", \"coordinates\":[[1,2], [3,4]]}') FROM src LIMIT 1;  -- constructs ST_LineString\n"
)

public class ST_GeomFromGeoJson extends GenericUDF {

    static final Log LOG = LogFactory.getLog(ST_GeomFromGeoJson.class.getName());

    ObjectInspector jsonOI;

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        DeferredObject jsonDeferredObject = arguments[0];

        String json = null;

        if (jsonOI.getCategory() == Category.STRUCT) {
            //StructObjectInspector structOI = (StructObjectInspector)jsonOI;

            // TODO support structs
        } else {
            PrimitiveObjectInspector primOI = (PrimitiveObjectInspector) jsonOI;
            json = (String) primOI.getPrimitiveJavaObject(jsonDeferredObject.get());
        }

        try {
            OGCGeometry ogcGeom = OGCGeometry.fromGeoJson(json);
            return GeometryUtils.geometryToEsriShapeBytesWritable(ogcGeom);
        } catch (Exception e) {
            LogUtils.Log_InvalidText(LOG, json);
        }

        return null;
    }

    @Override
    public String getDisplayString(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        String delim = "(";
        for (String arg : args) {
            sb.append(delim).append(arg);
            delim = ", ";
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments)
            throws UDFArgumentException {

        if (arguments.length != 1) {
            throw new UDFArgumentLengthException("ST_GeomFromJson takes only one argument");
        }

        ObjectInspector argJsonOI = arguments[0];

        if (argJsonOI.getCategory() == Category.PRIMITIVE) {
            PrimitiveObjectInspector poi = (PrimitiveObjectInspector) argJsonOI;

            if (poi.getPrimitiveCategory() != PrimitiveCategory.STRING) {
                throw new UDFArgumentTypeException(0, "ST_GeomFromJson argument category must be either a string primitive or struct");
            }
        } else if (argJsonOI.getCategory() != Category.STRUCT) {

        } else {
            throw new UDFArgumentTypeException(0, "ST_GeomFromJson argument category must be either a string primitive or struct");
        }

        jsonOI = argJsonOI;

        return GeometryUtils.geometryTransportObjectInspector;
    }

}
