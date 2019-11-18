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

import org.eclipse.collections.impl.list.mutable.FastList;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.misc.Assert;

import java.util.List;

/**
 * Spatial acceleration structure where the elements are lists
 */
public class SpatialListMap2d<Type> {
    public SpatialListMap2d(final Vector2d<Integer> size, final int gridsize) {
        Assert.Assert(gridsize != 0, "gridsize must be nonzero");

        Assert.Assert((size.x % gridsize) == 0, "size.x must be divisable by gridsize");
        Assert.Assert((size.y % gridsize) == 0, "size.y must be divisable by gridsize");

        this.gridsize = gridsize;
        this.map = new Map2d<>(size.x / gridsize, size.y / gridsize);
    }

    public Vector2d<Integer> getCellPositionOfIntegerPosition(final Vector2d<Integer> integerPosition) {
        return new Vector2d<>(integerPosition.x / gridsize, integerPosition.y / gridsize);
    }

    // /return can be null
    public List<Type> readAt(final int x, final int y) {
        return map.readAt(x, y);
    }

    public void clear() {
        int h = map.getLength();
        int w = map.getWidth();
        for(int y = 0; y < h; y++ ) {
            for(int x = 0; x < w; x++ ) {
                List<Type> l = map.readAt(x, y);
                if (l != null)
                    l.clear();
            }
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

    public List<Type> addAt(int x, int y, Type t) {
        List<Type> l = readAt(x, y);
        if (l == null) { setAt(x, y, l = new FastList()); }
        l.add(t);
        return l;
    }
}
