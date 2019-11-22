/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Algorithms;

import org.eclipse.collections.api.tuple.primitive.IntIntPair;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * Bresenham algorithms
 *
 * for circle see german version of
 * http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
 * which is
 * https://de.wikipedia.org/wiki/Bresenham-Algorithmus
 */
public enum Bresenham {
	;

	public interface IDrawer {
        void set(final IntIntPair position, final IntIntPair direction);
    }

    public static void rasterCircle(final IntIntPair position, final int radius, final IDrawer drawer) {

        final var positionx = position.getOne();
        final var positiony = position.getTwo();
        drawer.set(pair(positionx, positiony + radius), pair(0, 1));
        drawer.set(pair(positionx, positiony - radius), pair(0, -1));
        drawer.set(pair(positionx + radius, positiony), pair(1, 0));
        drawer.set(pair(positionx - radius, positiony), pair(-1, 0));

        var y = radius;
        var x = 0;
        var ddF_y = -2 * radius;
        var ddF_x = 0;
        var f = 1 - radius;
        while(x < y) {
            if(f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x + 1;

            drawer.set(pair(positionx + x, positiony + y), pair(0, 1)); // ok
            drawer.set(pair(positionx - x, positiony + y), pair(0, 1));
            drawer.set(pair(positionx + x, positiony - y), pair(0, -1));
            drawer.set(pair(positionx - x, positiony - y), pair(0, -1));
            drawer.set(pair(positionx + y, positiony + x), pair(1, 0));
            drawer.set(pair(positionx - y, positiony + x), pair(-1, 0));
            drawer.set(pair(positionx + y, positiony - x), pair(1, 0));
            drawer.set(pair(positionx - y, positiony - x), pair(-1, 0));
        }
    }

    public static void rasterLine(final IntIntPair a, final IntIntPair b, final IDrawer drawer) {
        // special cases
        final var ay = a.getTwo();
        final var by = b.getTwo();
        final var ax = a.getOne();
        final var bx = b.getOne();
        if( ay == by) {
            final int x1, x2;

            if( ax > bx) {
                x1 = b.getOne();
                x2 = a.getOne();
            }
            else {
                x1 = a.getOne();
                x2 = b.getOne();
            }

            for(var x = x1; x <= x2; x++ ) drawer.set(pair(x, ay), null);

            return;
        }

        if( ax == bx) {
            final int y1, y2;

            if( ay > by) {
                y1 = by;
                y2 = ay;
            }
            else {
                y1 = ay;
                y2 = by;
            }

            for(var y = y1; y <= y2; y++ ) drawer.set(pair(ax, y), null);

            return;
        }

        rasterLineBresenham(a, b, drawer);
    }

    private static void rasterLineBresenham(final IntIntPair a, final IntIntPair b, final IDrawer drawer) {
        final var dx = java.lang.Math.abs(b.getOne() - a.getOne());
        final var dy = -java.lang.Math.abs(b.getTwo() - a.getTwo());

        final var sx = signumWithoutZero(b.getOne() - a.getOne());
        final var sy = signumWithoutZero(b.getTwo() - a.getTwo());

        var err = dx+dy;

        var x = a.getOne();
        var y = a.getTwo();

        while (true) {
            drawer.set(pair(x, y), null);

            if( x == b.getOne() && y == b.getTwo() ) break;

            final var e2 = 2*err;

            if( e2 > dy ) {
                err += dy; x += sx;
            }

            if( e2 < dx ) {
                err += dx; y += sy;
            }
        }
    }

    private static int signumWithoutZero(final int value) {
        //if( value == 0 ) {
        //    return 0;
        //}
        return (value > 0) ? 1 : -1;
    }
}
