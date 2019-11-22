/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.visual;

import ptrman.math.Maths;

public class ColorRgb
{
    public final float r;
    public final float g;
    public final float b;

    public ColorRgb(final int rgb) {
        this((float)((rgb >> 16) & 255) / 255.0f, (float)((rgb >> 8) & 255) / 255.0f, (float)(rgb & 255) / 255.0f);
    }

    public ColorRgb(final float r, final float g, final float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float getScaledNormalizedMagnitude(final ColorRgb scale)
    {
        return (r*scale.r + g*scale.g + b*scale.b) / (scale.r+scale.g+scale.b);
    }

    public ColorRgb add(final ColorRgb other)
    {
        return new ColorRgb(r + other.r, g + other.g, b + other.b);
    }

    public ColorRgb sub(final ColorRgb other)
    {
        return new ColorRgb(r - other.r, g - other.g, b - other.b);
    }

    public ColorRgb mul(final ColorRgb other)
    {
        return new ColorRgb(r * other.r, g * other.g, b * other.b);
    }

    public double distanceSquared(final ColorRgb other)
    {
        return Maths.squaredDistance(new double[]{r, g, b});
    }

}


