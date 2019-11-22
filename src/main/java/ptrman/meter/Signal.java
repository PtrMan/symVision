/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ptrman.meter;

/**
 * Effectively a column header in a Metrics table; indicates what appears
 * in the column index of rows.
 * May cache re-usable metadata specific to the signal shared by several SignalData views (ex: min, max)
 */
public class Signal implements Comparable<Signal> {
    public final String id;
    public String unit;
    
    private double min, max;


    public Signal(final String id) {
        this(id, null);
    }

    public Signal(final String id, final String unit) {
        this.id = id;
        this.unit = unit;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return id.equals(((Signal)obj).id);
    }
    
    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(final Signal o) {
        return id.compareTo(o.id);
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
    
        void setMin(final double newMin) { this.min = newMin; }
        void setMax(final double newMax) { this.max = newMax; }

        void resetBounds() {
            min = Double.POSITIVE_INFINITY;
            max = Double.NEGATIVE_INFINITY;        
        }
//        void invalidateBounds() {
//            min = max = Double.NaN;
//        }
//
//        boolean isInvalidatedBounds() {
//            return (Double.isNaN(min));
//        }
//    

}
