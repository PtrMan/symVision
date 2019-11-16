package ptrman.Algorithms;

import ptrman.Datastructures.Vector2d;

/**
 * Bresenham algorithms
 *
 * for cirlce see german version of
 * http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
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
