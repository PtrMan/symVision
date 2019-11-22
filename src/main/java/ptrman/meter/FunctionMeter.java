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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Convenience implementation for a 1-signal meter
 */
public abstract class FunctionMeter<M> implements Meter<M> {

    private final List<Signal> signals;
    private M[] vector;

    public static String[] newDefaultSignalIDs(final String prefix, final int n) {
        final var s = IntStream.range(0, n).mapToObj(i -> prefix + '_' + i).toArray(String[]::new);
        return s;
    }
    public static String[] newDefaultSignalIDs(final String prefix, final String... prefixes) {
        final var s = Arrays.stream(prefixes).map(item -> prefix + '_' + item).toArray(String[]::new);
        return s;
    }
    
    public FunctionMeter(final String prefix, final int n) {
        this(newDefaultSignalIDs(prefix, n));
    }
    public FunctionMeter(final String prefix, final boolean noop, final String... prefixes) {
        this(newDefaultSignalIDs(prefix, prefixes));
    }
    
    public FunctionMeter(final String... ids) {
        final var s = Arrays.stream(ids).map(n -> new Signal(n, null)).collect(Collectors.toList());

        this.signals = Collections.unmodifiableList(s);
    }
    
    public void setUnits(final String... units) {
        var i = 0;
        for (final var s : signals)
            s.unit = units[i++];
    }

    @Override
    public List<Signal> getSignals() {
        return signals;
    }

    abstract protected M getValue(Object key, int index);

    protected void fillVector(final Object key, final int fromIndex, final int toIndex) {
        for (var i = 0; i < vector.length; i++) vector[i] = getValue(key, i);

    }

    @Override
    public M[] sample(final Object key) {
        //the following wont work because firstValue may be null
        //M firstValue = getValue(key, 0);
        //vector = (M[]) Array.newInstance(firstValue.getClass(), signals.size());
        if (vector == null) vector = (M[]) new Object[signals.size()];

        fillVector(key, 0, vector.length);
        

        return vector;
    }

}
