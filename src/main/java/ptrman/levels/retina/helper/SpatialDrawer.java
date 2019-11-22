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
import ptrman.Algorithms.Bresenham;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ptrman.Datastructures.Vector2d.IntegerHelper.add;
import static ptrman.Datastructures.Vector2d.IntegerHelper.getScaled;

/**
 * Used to draw circles in spatial datastructures
 */
public enum SpatialDrawer {
    ;

    static final private class Drawer implements Bresenham.IDrawer {

        public static class PositionWithDirection {
            private final IntIntPair position;
            private final IntIntPair direction;

            public PositionWithDirection(final IntIntPair position, final IntIntPair direction) {
                this.position = position;
                this.direction = direction;
            }
        }

        public final Collection<PositionWithDirection> positionWithDirections = new FastList<>();
        //public final List<Vector2d<Integer>> directions = new FastList<>();

        @Override
        public void set(final IntIntPair position, final IntIntPair direction) {
            positionWithDirections.add(new PositionWithDirection(position, direction));
        }
    }

    public static List<IntIntPair> getPositionsOfCellsOfCircleBound(final IntIntPair center, final int radius, final IntIntPair boundary) {
        final var drawer = new Drawer();

        Bresenham.rasterCircle(center, radius, drawer);

        final List<IntIntPair> resultPositions = new FastList<>(drawer.positionWithDirections.size());
        for (final var iterationPositionWithDirection : drawer.positionWithDirections) {
            final var position = iterationPositionWithDirection.position;
            if (isGridLocationInBound(position, boundary))
                resultPositions.add(position);
        }

        //resultPositions.removeIf(position -> !isGridLocationInBound(position, boundary));

        return resultPositions;
    }

    public static void getPositionsOfCellsWithNegativeDirectionOfCircleBound(final IntIntPair center, final int radius, final IntIntPair boundary, final Collection<IntIntPair> y) {
        final var drawer = new Drawer();

        Bresenham.rasterCircle(center, radius, drawer);

        for (final var iterationPositionWithDirection : drawer.positionWithDirections) {

            final var a = iterationPositionWithDirection.position;
            if (isGridLocationInBound(a, boundary)) y.add(a);

            final var b = add(a, getScaled(iterationPositionWithDirection.direction, -1));
            if (isGridLocationInBound(b, boundary))
                y.add(b);
        }

    }

    /** TODO stream */
    public static Iterable<IntIntPair> getPositionsOfCellsOfLineUnbound(final IntIntPair a, final IntIntPair b) {
        final var drawer = new Drawer();

        Bresenham.rasterLine(a, b, drawer);

        final Collection<IntIntPair> resultPositions = drawer.positionWithDirections.stream().map(iterationPositionWithDirection -> iterationPositionWithDirection.position).collect(Collectors.toCollection(() -> new ArrayList<>(drawer.positionWithDirections.size())));

        return resultPositions;
    }

    private static boolean isGridLocationInBound(final IntIntPair position, final IntIntPair boundary) {
        final var x = position.getOne();
        if (x >= 0 && x < boundary.getOne()) {
            final var y = position.getTwo();
            return y >= 0 && y < boundary.getTwo();
        }
        return false;
    }
}
