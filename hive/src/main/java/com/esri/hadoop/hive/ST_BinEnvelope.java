package com.esri.hadoop.hive;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.ogc.OGCPoint;
import com.esri.hadoop.hive.GeometryUtils.OGCType;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;

import java.util.EnumSet;

@Description(
        name = "ST_BinEnvelope",
        value = "_FUNC_(binsize, point) - return bin envelope for given point\n" +
                "_FUNC_(binsize, binid) - return bin envelope for given bin ID\n")
public class ST_BinEnvelope extends GenericUDF {
    private transient boolean binSizeIsConstant;
    private transient PrimitiveObjectInspector oiBinSize;
    private transient BinUtils bins;

    private transient PrimitiveObjectInspector oiBinId;
    private transient HiveGeometryOIHelper binPoint;

    @Override
    public ObjectInspector initialize(ObjectInspector[] OIs)
            throws UDFArgumentException {

        if (OIs.length != 2) {
            throw new UDFArgumentException("Function takes exactly 2 arguments");
        }

        if (!isPrimitiveNumber(OIs[0])) {
            throw new UDFArgumentException("Argument 0 must be a number");
        }

        oiBinSize = (PrimitiveObjectInspector) OIs[0];

        if (isPrimitiveNumber(OIs[1])) {
            oiBinId = (PrimitiveObjectInspector) OIs[1];
        } else if (HiveGeometryOIHelper.canCreate(OIs[1])) {
            binPoint = HiveGeometryOIHelper.create(OIs, 1);
        } else {
            throw new UDFArgumentException("Argument 1 must be a number or valid geometry type");
        }

        return GeometryUtils.geometryTransportObjectInspector;
    }

    private boolean isPrimitiveNumber(ObjectInspector oi) {
        if (oi.getCategory() != Category.PRIMITIVE) {
            return false;
        }

        return EnumSet.of(PrimitiveCategory.DOUBLE, PrimitiveCategory.INT, PrimitiveCategory.LONG,
                        PrimitiveCategory.SHORT, PrimitiveCategory.FLOAT, PrimitiveCategory.DECIMAL)
                .contains(((PrimitiveObjectInspector) oi).getPrimitiveCategory());
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        double binSize = PrimitiveObjectInspectorUtils.getDouble(args[0].get(), oiBinSize);


        if (!binSizeIsConstant || bins == null) {
            bins = new BinUtils(binSize);
        }

        Envelope env = new Envelope();

        if (oiBinId != null) {
            // argument 1 is a number, attempt to get the envelope with bin ID
            if (args[1].get() == null) {
                // null bin ID argument usually means the source point was null or failed to parse
                return null;
            }

            long binId = PrimitiveObjectInspectorUtils.getLong(args[1].get(), oiBinId);
            bins.queryEnvelope(binId, env);
        } else {
            // argument 1 is a geometry, attempt to get the envelope with a point
            OGCPoint point = binPoint.getPoint(args);

            if (point == null) {
                return null;
            }

            bins.queryEnvelope(point.X(), point.Y(), env);
        }

        return GeometryUtils.geometryToEsriShapeBytesWritable(env, 0, OGCType.ST_POLYGON);
    }

    @Override
    public String getDisplayString(String[] args) {
        assert (args.length == 2);
        return String.format("st_binenvelope(%s,%s)", args[0], args[1]);
    }
}
