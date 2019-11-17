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


import com.google.common.util.concurrent.AtomicDouble;

/**
 * Stores the latest provided value for retrieval by a Metrics 
 */
public class DoubleMeter extends SourceFunctionMeter<Double> {
    
    boolean autoReset;
    final AtomicDouble val = new AtomicDouble();
    private final String name;
    
    
    
    public DoubleMeter(String id, boolean autoReset) {
        super(id);
        this.name = id;
        this.autoReset = autoReset;
    }
    
    
    public DoubleMeter(String id) {
        this(id, false);
    }    


    public DoubleMeter reset() {
        set(Double.NaN);
        return this;
    }
    
    /** returns the previous value, or NaN if none were set  */
    public double set(double newValue) {
        double oldValue = val.get();
        val.set(newValue);
        return oldValue;
    }

    /** current stored value */
    public double get() { return val.get(); }

    
    @Override
    protected Double getValue(Object key, int index) {
        double c = val.get();
        if (autoReset) {
            reset();
        }
        return c;        
    }

    /** whether to reset to NaN after the count is next stored in the Metrics */
    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
    }
    
    public boolean getAutoReset() { return autoReset; }

    @Override
    public String toString() {
        return name + super.toString();
    }
}
