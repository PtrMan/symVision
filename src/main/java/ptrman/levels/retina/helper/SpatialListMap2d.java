package ptrman.levels.retina.helper;

import com.gs.collections.impl.list.mutable.FastList;
import com.sun.istack.internal.Nullable;
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

    @Nullable
    public List<Type> readAt(final int x, final int y) {
        List<Type> l = map.readAt(x, y);
        return l;
    }

    public void clear() {
        for( int y = 0; y < map.getLength(); y++ ) {
            for( int x = 0; x < map.getWidth(); x++ ) {
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


    private IMap2d<List<Type>> map;
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
