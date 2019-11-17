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


import ptrman.meter.Metrics;
import ptrman.meter.Signal;

import java.util.List;

/**
 * Computes the numeric first-order difference for the last 2 contiguous numeric
 * entries of a specific other column in a Metrics
 */
public class FirstOrderDifference extends DependsOnColumn {

    public FirstOrderDifference(Metrics metrics, String source) {
        super(metrics, source, 1);
        

    }

    @Override
    protected String getColumnID(Signal dependent, int i) {
        return dependent.id + ".change";
    }


    
    
    @Override
    protected Number getValue(Object key, int ignored) {
        
        List nv = newestValues(sourceColumn, 2);
                
        List<Double> values = Metrics.doubles(nv);
                
        if (values.size()<2) return null;
                
        double currentValue = values.get(0);
        double prevValue = values.get(1);
        
        return currentValue - prevValue;
    }

    
}
