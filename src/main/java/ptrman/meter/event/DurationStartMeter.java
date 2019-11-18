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

/**
 * Measures the period between start() and end() calls as a ValueMeter value.
 * Stores the starttime too
 */
public class DurationStartMeter extends DoubleMeter {

    private double storedStartTime = Double.NaN;
    private double startTime = Double.NaN;
    private final boolean nanoSeconds;
    //DescriptiveStatistics stat;
    private final double window;
    //private double prev;
    private final boolean frequency;
    private final boolean strict = false;

    public DurationStartMeter(String id, boolean nanoSeconds, double windowSec, boolean asFrequency) {
        super(id);


        this.window = windowSec * 1.0E9;
        //this.stat = new DescriptiveStatistics();
        this.nanoSeconds = nanoSeconds;
        this.frequency = asFrequency;
        reset();
    }

    public boolean isStarted() { return !Double.isNaN(startTime); }

    /** returns the stored start time of the event */
    public synchronized double start() {
        if (strict && isStarted()) {
            startTime = Double.NaN;
            throw new RuntimeException(this + " already started");
        }
        startTime = PeriodMeter.now(nanoSeconds);
        return startTime;
    }

    /** returns the value which it stores (duration time, or frequency) */
    public synchronized double stop() {
        if (strict && !isStarted())
            throw new RuntimeException(this + " not previously started");
        double duration = sinceStart();
        double v = frequency ? (1.0 / duration) : duration;
        set(v);
        storedStartTime = startTime;
        startTime = Double.NaN;
        return v;
    }

    public synchronized double sinceStart() {
        double resolutionTime = nanoSeconds ? 1.0E9 : 1.0E3;
        return (PeriodMeter.now(nanoSeconds) - startTime) / resolutionTime;
    }

    public double getStoredStartTime() {
        return startTime;
    }
}
