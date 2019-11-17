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

import org.eclipse.collections.api.map.primitive.IntBooleanMap;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public enum SetUtility {
    ;

    public static<Type> Type getRandomElementFromSet(final Set<Type> set, Random random) {
        final int index = random.nextInt(set.size());

        Iterator<Type> iterator = set.iterator();

        for( int i = 0; i < index-1; i++ ) {
            iterator.next();
        }

        return iterator.next();
    }

    public static int getRandomElementFromSet(final IntBooleanMap mappedKeys, Random random) {

        final int index = random.nextInt(mappedKeys.size());

        return mappedKeys.keysView().toArray()[index];
    }

}
