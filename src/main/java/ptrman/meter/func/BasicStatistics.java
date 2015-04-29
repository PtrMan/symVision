/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
