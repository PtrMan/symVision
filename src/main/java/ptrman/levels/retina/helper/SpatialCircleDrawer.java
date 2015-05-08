package ptrman.levels.retina.helper;

import ptrman.Algorithms.Bresenham;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to draw circles in spatial datastructures
 */
public final class SpatialCircleDrawer {
    static final private class Drawer implements Bresenham.IDrawer {
        public List<Vector2d<Integer>> positions = new ArrayList<>();

        @Override
        public void set(Vector2d<Integer> position) {
            positions.add(position);
        }
    }

    public static List<Vector2d<Integer>> getPositionsOfCellsOfCircle(final Vector2d<Integer> center, final int radius, final Vector2d<Integer> boundary) {
        Drawer drawer = new Drawer();

        Bresenham.rasterCircle(center, radius, drawer);

        drawer.positions.removeIf(position2 -> !isGridLocationInBound(position2, boundary));
        return drawer.positions;
    }

    private static boolean isGridLocationInBound(final Vector2d<Integer> position, final Vector2d<Integer> boundary) {
        return position.x >= 0 && position.x < boundary.x && position.y >= 0 && position.y < boundary.y;
    }
}
