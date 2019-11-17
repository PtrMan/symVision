/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Datastructures;

import static ptrman.misc.BitOperations.rotateLeft;

/**
 *
 */
public class HashableVector2dInteger extends Vector2d<Integer> {
    public static HashableVector2dInteger fromVector2dInteger(final Vector2d<Integer> argument) {
        return new HashableVector2dInteger(argument.x, argument.y);
    }

    public HashableVector2dInteger(final int x, final int y) {
        super(x, y);
    }

    @Override
    public int hashCode() {
        return rotateLeft(x, 5) ^ y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HashableVector2dInteger))
            return false;
        if (obj == this)
            return true;

        final HashableVector2dInteger rhs = (HashableVector2dInteger)obj;

        return x == rhs.x && y == rhs.y;
    }
}
