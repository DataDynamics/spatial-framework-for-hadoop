package com.esri.hadoop.hive;

import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCPoint;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

public class HiveGeometryOIHelper {

    static Logger LOG = Logger.getLogger(HiveGeometryOIHelper.class);
    OGCGeometry constantGeometry;
    private PrimitiveObjectInspector oi;
    private int argIndex;
    private boolean isConstant;
    private BytesWritable last = null;
    // always assume bytes are reused until we determine they aren't
    private boolean bytesReused = true;

    private HiveGeometryOIHelper(ObjectInspector oi, int argIndex) {
        this.oi = (PrimitiveObjectInspector) oi;
        this.argIndex = argIndex;

        // constant geometries only need to be processed once and can
        // be optimized in certain operations
        isConstant = ObjectInspectorUtils.isConstantObjectInspector(oi);
    }

    public static HiveGeometryOIHelper create(ObjectInspector[] OIs, int argIndex) throws UDFArgumentException {
        return create(OIs[argIndex], argIndex);
    }

    public static HiveGeometryOIHelper create(ObjectInspector oi, int argIndex) throws UDFArgumentException {
        if (oi.getCategory() != Category.PRIMITIVE) {
            throw new UDFArgumentException("Geometry argument must be a primitive type");
        }

        return new HiveGeometryOIHelper(oi, argIndex);
    }

    public static boolean canCreate(ObjectInspector oi) {
        if (oi.getCategory() != Category.PRIMITIVE) {
            return false;
        }

        return true;
    }

    /**
     * Gets whether this geometry argument is constant.
     *
     * @return
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * Returns the cached constant geometry object.
     *
     * @return cache geometry, or null if not constant
     */
    public OGCGeometry getConstantGeometry() {
        return constantGeometry;
    }

    /**
     * Reads the corresponding geometry from the deferred object list
     * or returns the cached geometry if argument is constant.
     *
     * @param args
     * @return OGCPoint or null if not a point
     * @see #getGeometry(DeferredObject[])
     */
    public OGCPoint getPoint(DeferredObject[] args) {
        OGCGeometry geometry = getGeometry(args);

        if (geometry instanceof OGCPoint) {
            return (OGCPoint) geometry;
        } else {
            return null;
        }
    }

    /**
     * Reads the corresponding geometry from the deferred object list
     * or returns the cached geometry if argument is constant.
     *
     * @param args
     * @return
     */
    public OGCGeometry getGeometry(DeferredObject[] args) {
        if (isConstant) {
            if (constantGeometry == null) {
                constantGeometry = getGeometry(args[argIndex]);
            }

            return constantGeometry;
        } else {
            // not constant, so we have to rebuild the geometry
            // on every call
            return getGeometry(args[argIndex]);
        }
    }

    private OGCGeometry getGeometry(DeferredObject arg) {
        Object writable;
        try {
            writable = oi.getPrimitiveWritableObject(arg.get());
        } catch (HiveException e) {
            LOG.error("Failed to get writable", e);
            return null;
        }

        if (writable == null) {
            return null;
        }

        switch (oi.getPrimitiveCategory()) {
            case BINARY:
                return getGeometryFromBytes((BytesWritable) writable);
            case STRING:
                return OGCGeometry.fromText(((Text) writable).toString());
            default:
                return null;
        }
    }

    private OGCGeometry getGeometryFromBytes(BytesWritable writable) {

        if (bytesReused) {
            if (last != null && last != writable) {
                // this assumes that the source of these bytes will either always
                // reuse the bytes or never reuse the bytes.
                bytesReused = false;
            }
            last = writable;
        }

        return GeometryUtils.geometryFromEsriShape((BytesWritable) writable, bytesReused);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("HiveGeometryHelper(");
        builder.append("constant=" + isConstant + ";");
        builder.append(")");

        return builder.toString();
    }
}