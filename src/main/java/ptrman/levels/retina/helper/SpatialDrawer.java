package ptrman.levels.retina.helper;

import ptrman.Algorithms.Bresenham;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to draw circles in spatial datastructures
 */
public final class SpatialDrawer {
    static final private class Drawer implements Bresenham.IDrawer {
        public List<Vector2d<Integer>> positions = new ArrayList<>();

        @Override
        public void set(Vector2d<Integer> position) {
            positions.add(position);
        }
    }

    public static List<Vector2d<Integer>> getPositionsOfCellsOfCircleBound(final Vector2d<Integer> center, final int radius, final Vector2d<Integer> boundary) {
        Drawer drawer = new Drawer();

        Bresenham.rasterCircle(center, radius, drawer);

        drawer.positions.removeIf(position2 -> !isGridLocationInBound(position2, boundary));
        return drawer.positions;


        // played SUPERCOMPILATION by hand and inlined the bresenham algorithm
        /*
        List<Vector2d<Integer>> positions = new ArrayList<>();

        int f = 1 - radius;
        int ddF_x = 0;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        positions.add(new Vector2d<>(center.x, center.y + radius));
        positions.add(new Vector2d<>(center.x, center.y - radius));
        positions.add(new Vector2d<>(center.x + radius, center.y));
        positions.add(new Vector2d<>(center.x - radius, center.y));

        while(x < y) {
            if(f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x + 1;

            positions.add(new Vector2d<>(center.x + x, center.y + y));
            positions.add(new Vector2d<>(center.x + x, center.y - y));
            positions.add(new Vector2d<>(center.x - x, center.y + y));
            positions.add(new Vector2d<>(center.x - x, center.y - y));
        }


        positions.removeIf(position2 -> !isGridLocationInBound(position2, boundary));

        return positions;
        */
    }

    private static boolean isGridLocationInBound(final Vector2d<Integer> position, final Vector2d<Integer> boundary) {
        return position.x >= 0 && position.x < boundary.x && position.y >= 0 && position.y < boundary.y;
    }
}
