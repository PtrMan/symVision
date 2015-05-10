package ptrman.levels.retina.helper;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.misc.Assert;

import java.util.ArrayList;
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

    public List<Type> readAt(final int x, final int y) {
        return map.readAt(x, y);
    }

    public void clean() {
        for( int y = 0; y < map.getLength(); y++ ) {
            for( int x = 0; x < map.getWidth(); x++ ) {
                map.setAt(x, y, new ArrayList<>());
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
}
