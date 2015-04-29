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
    private boolean strict = false;

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
