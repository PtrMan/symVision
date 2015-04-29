/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.nars.io.meter.event;

import main.java.nars.io.meter.FunctionMeter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Measures the period between hit() calls to determine frequency-related
 * statistics of hit intervals.
 * millisecond resolution performs better than nanoseconds but both are
 * converted to a double value (nanosecond unixtime) for calculation.
 * 
 * the results can be returned either as period duration (sec) or frequency (1/period).
 */
public class PeriodMeter extends FunctionMeter<Double> {
    
    double lastReset;
    private final boolean nanoSeconds;
    final DescriptiveStatistics stat;
    private final double window;
    private double prev;
    private final boolean frequency;
    
    public PeriodMeter(String id, boolean nanoSeconds, double windowSec, boolean asFrequency) {
        super(id, true, ".min", ".max", ".mean", ".stddev");
        

        this.window = windowSec * 1.0E9;
        this.stat = new DescriptiveStatistics();
        this.nanoSeconds = nanoSeconds;
        this.frequency = asFrequency;
        reset();
    }
    
     public static double now(boolean nanoSeconds /* TODO use a Resolution enum */) {
        if (nanoSeconds) {
            return (double)System.nanoTime();
        }
        else {
            return System.currentTimeMillis() * 1.0E6;
        }
    }

    public double sinceStart() {
        return now(nanoSeconds) - lastReset;
    }
    

    public double reset() {
        this.lastReset = now(nanoSeconds);
        stat.clear();
        return lastReset;
    }
    
    public DescriptiveStatistics hit() {
        double now;
        if (sinceStart() > window) {
            now = reset();
        }
        else {
            now = now(nanoSeconds);
        }
        if (Double.isFinite(this.prev)) {
            double dt = now - this.prev;
            stat.addValue(dt);
        }
        this.prev = now;
        return stat;
    }
    
    
    @Override
    protected Double getValue(Object key, int index) {
        if (stat.getN() == 0) return null;
        switch (index) {
            case 0: return f(stat.getMin());
            case 1: return f(stat.getMax());
            case 2: return f(stat.getMean());
            case 4: return stat.getStandardDeviation();
        }
        return null;
    }
    
    protected double f(double period) {
        if (frequency) {
            if (period == 0) return Double.POSITIVE_INFINITY;
            return 1.0/period;
        }            
        return period;
    }

    
}
