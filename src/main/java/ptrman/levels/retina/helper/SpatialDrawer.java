package ptrman.levels.retina.helper;

import com.gs.collections.impl.list.mutable.FastList;
import ptrman.Algorithms.Bresenham;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static ptrman.Datastructures.Vector2d.IntegerHelper.add;
import static ptrman.Datastructures.Vector2d.IntegerHelper.getScaled;

/**
 * Used to draw circles in spatial datastructures
 */
public final class SpatialDrawer {
    static final private class Drawer implements Bresenham.IDrawer {

        public static class PositionWithDirection {
            private final Vector2d<Integer> position;
            private final Vector2d<Integer> direction;

            public PositionWithDirection(final Vector2d<Integer> position, final Vector2d<Integer> direction) {
                this.position = position;
                this.direction = direction;
            }
        }

        public final List<PositionWithDirection> positionWithDirections = new FastList<>();
        public final List<Vector2d<Integer>> directions = new FastList<>();

        @Override
        public void set(final Vector2d<Integer> position, final Vector2d<Integer> direction) {
            positionWithDirections.add(new PositionWithDirection(position, direction));
        }
    }

    public static List<Vector2d<Integer>> getPositionsOfCellsOfCircleBound(final Vector2d<Integer> center, final int radius, final Vector2d<Integer> boundary) {
        Drawer drawer = new Drawer();

        Bresenham.rasterCircle(center, radius, drawer);

        List<Vector2d<Integer>> resultPositions = new FastList<>(drawer.positionWithDirections.size());
        for (final Drawer.PositionWithDirection iterationPositionWithDirection : drawer.positionWithDirections) {
            Vector2d<Integer> position = iterationPositionWithDirection.position;
            if (isGridLocationInBound(position, boundary))
                resultPositions.add(position);
        }

        //resultPositions.removeIf(position -> !isGridLocationInBound(position, boundary));

        return resultPositions;
    }

    public static List<Vector2d<Integer>> getPositionsOfCellsWithNegativeDirectionOfCircleBound(final Vector2d<Integer> center, final int radius, final Vector2d<Integer> boundary) {
        Drawer drawer = new Drawer();

        Bresenham.rasterCircle(center, radius, drawer);

        List<Vector2d<Integer>> resultPositions = new ArrayList<>();
        for (final Drawer.PositionWithDirection iterationPositionWithDirection : drawer.positionWithDirections) {
            resultPositions.add(iterationPositionWithDirection.position);
            resultPositions.add(add(iterationPositionWithDirection.position, getScaled(iterationPositionWithDirection.direction, -1)));
        }

        resultPositions.removeIf(position -> !isGridLocationInBound(position, boundary));

        return resultPositions;
    }

    public static List<Vector2d<Integer>> getPositionsOfCellsOfLineUnbound(final Vector2d<Integer> a, final Vector2d<Integer> b) {
        Drawer drawer = new Drawer();

        Bresenham.rasterLine(a, b, drawer);

        List<Vector2d<Integer>> resultPositions = new ArrayList<>();
        for (final Drawer.PositionWithDirection iterationPositionWithDirection : drawer.positionWithDirections) {
            resultPositions.add(iterationPositionWithDirection.position);
        }
        return resultPositions;
    }

    private static boolean isGridLocationInBound(final Vector2d<Integer> position, final Vector2d<Integer> boundary) {
        return position.x >= 0 && position.x < boundary.x && position.y >= 0 && position.y < boundary.y;
    }
}
