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

import java.util.List;

/** produces 1 or more signals */
public interface Meter<M> {

    
    default void setActive(boolean active) {
    }

    /** the list of signals produced by this meter; this should not change
     * in quantity during operation
     */
	List<Signal> getSignals();
    
    default Signal signal(int i) {
        return getSignals().get(i);
    }
    default String signalID(int i) {
        return getSignals().get(i).id;
    }
    
    /** convenience method for accessing the first of the signals, in case one needs the only signal */
    default Signal signalFirst() {
        return getSignals().get(0);
    }
    

    /**
     * @param key the current row's leading element, usually time
     * @return the values described by the signals, 
     * or null (no data). if any of the elements are null, that column's data 
     * point is not recorded (ie. NaN).
     */
	M[] sample(Object key);
    
    default int numSignals() { return getSignals().size(); }
    
    
}
