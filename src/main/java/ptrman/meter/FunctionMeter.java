/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.meter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convenience implementation for a 1-signal meter
 */
public abstract class FunctionMeter<M> implements Meter<M> {

    private final List<Signal> signals;
    private M[] vector;

    public static String[] newDefaultSignalIDs(String prefix, int n) {
        String[] s = new String[n];
        for (int i = 0; i < n; i++)
            s[i] = prefix + '_' + i;
        return s;
    }
    public static String[] newDefaultSignalIDs(String prefix, String... prefixes) {
        String[] s = new String[prefixes.length];
        for (int i = 0; i < prefixes.length; i++)
            s[i] = prefix + '_' + prefixes[i];
        return s;
    }
    
    public FunctionMeter(String prefix, int n) {
        this(newDefaultSignalIDs(prefix, n));
    }
    public FunctionMeter(String prefix, boolean noop, String... prefixes) {
        this(newDefaultSignalIDs(prefix, prefixes));
    }
    
    public FunctionMeter(String... ids) {
        List<Signal> s = new ArrayList();
        for (String n : ids) {
            s.add(new Signal(n, null));
        }

        this.signals = Collections.unmodifiableList(s);
    }
    
    public void setUnits(String... units) { 
        int i = 0;
        for (Signal s : signals)
            s.unit = units[i++];
    }

    @Override
    public List<Signal> getSignals() {
        return signals;
    }

    abstract protected M getValue(Object key, int index);

    protected void fillVector(Object key, int fromIndex, int toIndex) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = getValue(key, i);
        }

    }

    @Override
    public M[] sample(Object key) {
        if (vector == null) {
            //the following wont work because firstValue may be null
            //M firstValue = getValue(key, 0);            
            //vector = (M[]) Array.newInstance(firstValue.getClass(), signals.size());
            vector = (M[]) new Object[signals.size()];

        }

        fillVector(key, 0, vector.length);
        

        return vector;
    }

}
