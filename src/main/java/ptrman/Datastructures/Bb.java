/**
 * Copyright 2020 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Datastructures;

/**
 * Bounding box
 */
public class Bb {
    public void add(double x, double y) {
        minx = Math.min(minx, x);
        maxx = Math.max(maxx, x);
        miny = Math.min(miny, y);
        maxy = Math.max(maxy, y);
    }

    public boolean in(double x, double y) {
        return minx <= x && maxx >= x && miny <= y && maxy >= y;
    }

    // helper
    public static boolean inRange(Bb bb, double x, double y, double maxDist) {
        // virtual BB
        Bb bb2 = new Bb();
        bb2.maxx = bb.maxx+maxDist;
        bb2.minx = bb.minx-maxDist;
        bb2.maxy = bb.maxy+maxDist;
        bb2.miny = bb.miny-maxDist;
        return bb2.in(x,y);
    }

    public static Bb merge(Bb a, Bb b) {
        Bb res = new Bb();
        res.add(a.minx, a.miny);
        res.add(a.maxx, a.maxy);
        res.add(b.minx, b.miny);
        res.add(b.maxx, b.maxy);
        return res;
    }

    public static boolean checkOverlap(Bb a, Bb b) {
        return overlapRange(a.minx, a.maxx, b.minx, b.maxx) && overlapRange(a.miny, a.maxy, b.miny, b.maxy);
    }

    // stupid helper
    public static boolean overlapRange(double mina, double maxa, double minb, double maxb) {
        if (mina<minb && minb<maxa) {
            return true;
        }
        if (mina<maxb && maxb<maxa) {
            return true;
        }

        if (minb<mina && mina<maxb) {
            return true;
        }
        if (minb<maxa && maxa<maxb) {
            return true;
        }

        if (mina<minb && maxb<maxa) {
            return true;
        }

        if (minb<mina && maxa<maxb) {
            return true;
        }

        return false;
    }

    public double minx = Double.POSITIVE_INFINITY;
    public double maxx = Double.NEGATIVE_INFINITY;
    public double miny = Double.POSITIVE_INFINITY;
    public double maxy = Double.NEGATIVE_INFINITY;
}
