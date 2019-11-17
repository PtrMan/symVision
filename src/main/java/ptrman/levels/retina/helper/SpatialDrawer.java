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
import ptrman.Algorithms.Bresenham;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static ptrman.Datastructures.Vector2d.IntegerHelper.add;
import static ptrman.Datastructures.Vector2d.IntegerHelper.getScaled;

/**
 * Used to draw circles in spatial datastructures
 */
public enum SpatialDrawer {
    ;

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
        //public final List<Vector2d<Integer>> directions = new FastList<>();

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

        List<Vector2d<Integer>> resultPositions = new FastList<>(drawer.positionWithDirections.size());
        for (final Drawer.PositionWithDirection iterationPositionWithDirection : drawer.positionWithDirections) {

            final Vector2d<Integer> a = iterationPositionWithDirection.position;
            if (isGridLocationInBound(a, boundary))
                resultPositions.add(a);

            final Vector2d<Integer> b = add(a, getScaled(iterationPositionWithDirection.direction, -1));
            if (isGridLocationInBound(b, boundary))
                resultPositions.add(b);
        }


        return resultPositions;
    }

    public static List<Vector2d<Integer>> getPositionsOfCellsOfLineUnbound(final Vector2d<Integer> a, final Vector2d<Integer> b) {
        Drawer drawer = new Drawer();

        Bresenham.rasterLine(a, b, drawer);

        List<Vector2d<Integer>> resultPositions = new ArrayList<>(drawer.positionWithDirections.size());
        for (final Drawer.PositionWithDirection iterationPositionWithDirection : drawer.positionWithDirections) {
            resultPositions.add(iterationPositionWithDirection.position);
        }
        return resultPositions;
    }

    private static boolean isGridLocationInBound(final Vector2d<Integer> position, final Vector2d<Integer> boundary) {
        final int x = position.xInt();
        final int y = position.yInt();
        return x >= 0 && x < boundary.x && y >= 0 && y < boundary.y;
    }
}
