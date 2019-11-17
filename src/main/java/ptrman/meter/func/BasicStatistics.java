/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.meter.func;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import ptrman.meter.Metrics;
import ptrman.meter.Signal;

/**
 * Uses apache commons math 
 */
public class BasicStatistics extends DependsOnColumn<Number,Double> {
    
    private StatisticalSummary stat;

    /** no window, uses SummaryStatistics */
    public BasicStatistics(Metrics metrics, String derivedFrom) {
        this(metrics, derivedFrom, 0);        
    }
    
    /** fixed window if windowSize>0 (in seconds, or whatever time unit is applied) , uses DescriptiveStatistics */
    public BasicStatistics(Metrics metrics, String derivedFrom, int windowSize) {
        super(metrics, derivedFrom, 2);
        
        setWindowSize(windowSize);
    }
    
    public void setWindowSize(int w) {
        if (w == 0)
            stat = new SummaryStatistics();
        else
            stat = new DescriptiveStatistics(w);
    }

    
    
    @Override
    protected String getColumnID(Signal dependent, int i) {
        switch (i) {
            case 0: return dependent.id + ".mean";
            case 1: return dependent.id + ".stdev";
        }
        return null;
    }

    @Override
    protected Double getValue(Object key, int index) {
        
        if (index == 0) {
            double nextValue = newestValue().doubleValue();
            if (Double.isFinite(nextValue)) {
                if (stat instanceof SummaryStatistics)
                    ((SummaryStatistics)stat).addValue( nextValue );    
                else if (stat instanceof DescriptiveStatistics)
                    ((DescriptiveStatistics)stat).addValue( nextValue );
            }
        }
        
        switch (index) {
            case 0: return stat.getMean();
            case 1: return stat.getStandardDeviation();
        }
        
        return null;
    }
    
}
