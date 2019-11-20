/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.meter.event;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ptrman.meter.FunctionMeter;

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
         return nanoSeconds ? (double) System.nanoTime() : System.currentTimeMillis() * 1.0E6;
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
        now = sinceStart() > window ? reset() : now(nanoSeconds);
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
