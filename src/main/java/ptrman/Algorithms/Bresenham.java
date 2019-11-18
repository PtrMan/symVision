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
import ptrman.Datastructures.Vector2d;

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

    public static void rasterCircle(final IntIntPair position, final int radius, IDrawer drawer) {
        int f = 1 - radius;
        int ddF_x = 0;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        int positionx = position.getOne(), positiony = position.getTwo();
        drawer.set(pair(positionx, positiony + radius), pair(0, 1));
        drawer.set(pair(positionx, positiony - radius), pair(0, -1));
        drawer.set(pair(positionx + radius, positiony), pair(1, 0));
        drawer.set(pair(positionx - radius, positiony), pair(-1, 0));

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

    public static void rasterLine(final IntIntPair a, final IntIntPair b, IDrawer drawer) {
        // special cases
        int ay = a.getTwo();
        int by = b.getTwo();
        int ax = a.getOne();
        int bx = b.getOne();
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

            for( int x = x1; x <= x2; x++ ) {
                drawer.set(pair(x, ay), null);
            }

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

            for( int y = y1; y <= y2; y++ ) {
                drawer.set(pair(ax, y), null);
            }

            return;
        }

        rasterLineBresenham(a, b, drawer);
    }

    private static void rasterLineBresenham(final IntIntPair a, final IntIntPair b, IDrawer drawer) {
        final int dx = java.lang.Math.abs(b.getOne() - a.getOne());
        final int dy = -java.lang.Math.abs(b.getTwo() - a.getTwo());

        final int sx = signumWithoutZero(b.getOne() - a.getOne());
        final int sy = signumWithoutZero(b.getTwo() - a.getTwo());

        int err = dx+dy;

        int x = a.getOne();
        int y = a.getTwo();

        for(;;) {
            drawer.set(pair(x, y), null);

            if( x == b.getOne() && y == b.getTwo() ) {
                break;
            }

            int e2 = 2*err;

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
