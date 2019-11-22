/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina.helper;

import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.list.mutable.FastList;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;

import java.util.List;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * Spatial acceleration structure where the elements are lists
 */
public class SpatialListMap2d<Type> {
    public SpatialListMap2d(final Vector2d<Integer> size, final int gridsize) {
        assert gridsize != 0 : "ASSERT: " + "gridsize must be nonzero";

        assert (size.x % gridsize) == 0 : "ASSERT: " + "size.x must be divisable by gridsize";
        assert (size.y % gridsize) == 0 : "ASSERT: " + "size.y must be divisable by gridsize";

        this.gridsize = gridsize;
        this.map = new Map2d<>(size.x / gridsize, size.y / gridsize);
    }

    public IntIntPair getCellPositionOfIntegerPosition(final IntIntPair integerPosition) {
        return pair(integerPosition.getOne() / gridsize, integerPosition.getTwo() / gridsize);
    }

    // /return can be null
    public List<Type> readAt(final int x, final int y) {
        return map.readAt(x, y);
    }

    public void clear() {
        final var h = map.getLength();
        final var w = map.getWidth();
        for(var y = 0; y < h; y++ )
            for (var x = 0; x < w; x++) {
                final var l = map.readAt(x, y);
                if (l != null)
                    l.clear();
            }
    }

    public int getWidth() {
        return map.getWidth();
    }

    public int getLength() {
        return map.getLength();
    }


    private final IMap2d<List<Type>> map;
    private final int gridsize;

    public void setAt(final int x, final int y, final List<Type> data) {
        map.setAt(x, y, data);
    }

    public boolean inBounds(final Vector2d<Integer> position) {
        return map.inBounds(position);
    }
    public boolean inBounds(final int x, final int y) {
        return x >= 0 && y >=0 &&  x < map.getWidth() && y < map.getLength();
    }

    public List<Type> addAt(final int x, final int y, final Type t) {
        var l = readAt(x, y);
        if (l == null) setAt(x, y, l = new FastList());
        l.add(t);
        return l;
    }
}
