/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ptrman.meter.func;


import ptrman.meter.FunctionMeter;
import ptrman.meter.Metrics;
import ptrman.meter.Signal;

import java.util.Iterator;
import java.util.List;

/**
 * @param Source Return type
 */
abstract public class DependsOnColumn<Source,Result> extends FunctionMeter<Result> {

    protected final int sourceColumn;
    protected final Metrics metrics;
    

    //TODO since making id final, getcolumnID will have no effect. replace with a static builder method or something

    public DependsOnColumn(Metrics metrics, String source, int numResults) {
        super("", numResults);
        
        int i = 0;
        Signal m = metrics.getSignal(source);
        if (m == null)
            throw new RuntimeException("Missing signal: " + source);

        this.metrics = metrics;
        this.sourceColumn = metrics.indexOf(source);

        for (Signal s : getSignals()) {            
            //s.id = getColumnID(m, i++);
        }
        
    }

//    
    /*public Iterator<Object> signalIterator() {
        return metrics.getSignalIterator(sourceColumn, true);        
    }*/
    
    public Source newestValue() { 
        Iterator<Object[]> r = metrics.reverseIterator();
        if (r.hasNext())
            return (Source) r.next()[sourceColumn];
        return null;
    }
    //public List<Object> newestValues(int n) { 

    protected List<Object> newestValues(int column, int i) {
        return metrics.getNewSignalValues(column, i);
    }
    
    abstract protected String getColumnID(Signal dependent, int i);
    
}
