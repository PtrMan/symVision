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
