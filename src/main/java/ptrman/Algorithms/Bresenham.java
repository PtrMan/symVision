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

        /**
         *
         * .set(new Vector2d<>(position.x + x, position.y + y));
         * .set(new Vector2d<>(position.x - x, position.y + y));
         * .set(new Vector2d<>(position.x + x, position.y - y));
         * .set(new Vector2d<>(position.x - x, position.y - y));
         *
         */
        void setAllDirections(final int centerX, final int centerY, final int x, final int y);
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

            drawer.setAllDirections(position.x, position.y, x, y);
        }
    }
}
