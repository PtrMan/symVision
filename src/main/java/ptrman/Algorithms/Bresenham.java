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

import ptrman.Datastructures.Vector2d;

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
        void set(final Vector2d<Integer> position, final Vector2d<Integer> direction);
    }

    public static void rasterCircle(final Vector2d<Integer> position, final int radius, IDrawer drawer) {
        int f = 1 - radius;
        int ddF_x = 0;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        drawer.set(new Vector2d<>(position.x, position.y + radius), new Vector2d<>(0, 1));
        drawer.set(new Vector2d<>(position.x, position.y - radius), new Vector2d<>(0, -1));
        drawer.set(new Vector2d<>(position.x + radius, position.y), new Vector2d<>(1, 0));
        drawer.set(new Vector2d<>(position.x - radius, position.y), new Vector2d<>(-1, 0));

        while(x < y) {
            if(f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x + 1;

            drawer.set(new Vector2d<>(position.x + x, position.y + y), new Vector2d<>(0, 1)); // ok
            drawer.set(new Vector2d<>(position.x - x, position.y + y), new Vector2d<>(0, 1));
            drawer.set(new Vector2d<>(position.x + x, position.y - y), new Vector2d<>(0, -1));
            drawer.set(new Vector2d<>(position.x - x, position.y - y), new Vector2d<>(0, -1));
            drawer.set(new Vector2d<>(position.x + y, position.y + x), new Vector2d<>(1, 0));
            drawer.set(new Vector2d<>(position.x - y, position.y + x), new Vector2d<>(-1, 0));
            drawer.set(new Vector2d<>(position.x + y, position.y - x), new Vector2d<>(1, 0));
            drawer.set(new Vector2d<>(position.x - y, position.y - x), new Vector2d<>(-1, 0));
        }
    }

    public static void rasterLine(final Vector2d<Integer> a, final Vector2d<Integer> b, IDrawer drawer) {
        // special cases
        if( a.y == b.y ) {
            final int x1, x2;

            if( a.x > b.x ) {
                x1 = b.x;
                x2 = a.x;
            }
            else {
                x1 = a.x;
                x2 = b.x;
            }

            for( int x = x1; x <= x2; x++ ) {
                drawer.set(new Vector2d<>(x, a.y), null);
            }

            return;
        }

        if( a.x == b.x ) {
            final int y1, y2;

            if( a.y > b.y ) {
                y1 = b.y;
                y2 = a.y;
            }
            else {
                y1 = a.y;
                y2 = b.y;
            }

            for( int y = y1; y <= y2; y++ ) {
                drawer.set(new Vector2d<>(a.x, y), null);
            }

            return;
        }

        rasterLineBresenham(a, b, drawer);
    }

    private static void rasterLineBresenham(final Vector2d<Integer> a, final Vector2d<Integer> b, IDrawer drawer) {
        final int dx = java.lang.Math.abs(b.x - a.x);
        final int dy = -java.lang.Math.abs(b.y - a.y);

        final int sx = signumWithoutZero(b.x - a.x);
        final int sy = signumWithoutZero(b.y - a.y);

        int err = dx+dy;

        int x = a.x;
        int y = a.y;

        for(;;) {
            drawer.set(new Vector2d<>(x, y), null);

            if( x == b.x && y == b.y ) {
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
        if( value > 0 ) {
            return  1;
        }
        else {
            return -1;
        }
    }
}
