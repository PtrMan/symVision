package ptrman.Algorithms;

import ptrman.Datastructures.Vector2d;

/**
 * Bresenham algorithms
 *
 * for cirlce see german version of
 * http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
 */
public class Bresenham {
    public interface IDrawer {
        void set(final Vector2d<Integer> position);
    }

    public static void rasterCircle(final Vector2d<Integer> position, final int radius, IDrawer drawer) {
        int f = 1 - radius;
        int ddF_x = 0;
        int ddF_y = -2 * radius;
        int x = 0;
        int y = radius;

        drawer.set(new Vector2d<>(position.x, position.y + radius));
        drawer.set(new Vector2d<>(position.x, position.y - radius));
        drawer.set(new Vector2d<>(position.x + radius, position.y));
        drawer.set(new Vector2d<>(position.x - radius, position.y));

        while(x < y) {
            if(f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x + 1;

            drawer.set(new Vector2d<>(position.x + x, position.y + y));
            drawer.set(new Vector2d<>(position.x - x, position.y + y));
            drawer.set(new Vector2d<>(position.x + x, position.y - y));
            drawer.set(new Vector2d<>(position.x - x, position.y - y));
        }
    }

    public static void rasterLine(final Vector2d<Integer> a, final Vector2d<Integer> b, IDrawer drawer) {
        int dx = java.lang.Math.abs(b.x-a.x);
        int dy = java.lang.Math.abs(b.y-a.y);

        int sx = a.x > b.x ? 1 : -1;
        int sy = a.y > b.y ? 1 : -1;

        int err = dx+dy;

        int x = a.x;
        int y = a.y;

        for(;;) {
            drawer.set(new Vector2d<>(x, y));

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
}
