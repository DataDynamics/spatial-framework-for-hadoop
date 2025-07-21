package com.esri.hadoop.hive;

import org.apache.hadoop.io.WritableComparator;

public class DoubleWritable extends org.apache.hadoop.io.DoubleWritable {
    static {
        WritableComparator.define(DoubleWritable.class, new org.apache.hadoop.io.DoubleWritable.Comparator());
    }

    public DoubleWritable() {
    }

    public DoubleWritable(double value) {
        super(value);
    }
}
