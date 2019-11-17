/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.misc;

import java.util.List;

import static ptrman.misc.BitOperations.rotateLeft;

/**
 *
 */
public class HashableIntegerList {
    public HashableIntegerList(List<Integer> list) {
        this.list = list;
    }

    @Override
    public int hashCode() {
        int result = 0;

        for( final int iterationInteger : list ) {
            result = result ^ iterationInteger;
            result = rotateLeft(result, 6);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HashableIntegerList))
            return false;
        if (obj == this)
            return true;

        HashableIntegerList rhs = (HashableIntegerList) obj;

        if( rhs.list.size() != list.size() ) {
            return false;
        }

        for( int i = 0; i < list.size(); i++ ) {
            if( rhs.list.get(i) != list.get(i) ) {
                return false;
            }
        }

        return true;
    }

    private List<Integer> list;
}
