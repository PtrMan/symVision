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

import ptrman.Datastructures.IMap2d;

import java.util.Arrays;

/**
 * Created by me on 7/17/15.
 */
public class FloatMap2d implements IMap2d<Float> {
    private final int w;
    private final int h;
    public final float[][] data;

    public FloatMap2d(final int w, final int h) {
        this.w = w;
        this.h = h;
        this.data = new float[h][];
        for (var i = 0; i < h; i++) data[i] = new float[w];
    }

    public FloatMap2d(final FloatMap2d f) {
        throw new RuntimeException("not impl yet");
        //this.w = f.w;
        //this.h = f.h;
        //this.data = f.data; //TODO copy 2d array
    }

    @Override
    public void clear() {
        for (final var d : data)
            Arrays.fill(d, 0);
    }


    @Override
    public Float readAt(final int x, final int y) {
        return get(x, y);
    }

    public float get(final int x, final int y) {
        return data[y][x];
    }

    @Override
    public void setAt(final int x, final int y, final Float value) {
        set(x, y, value);
    }

    public void set(final int x, final int y, final float v) {
        data[y][x] = v;
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getLength() {
        return h;
    }


    @Override
    public FloatMap2d copy() {
        return new FloatMap2d(this);
    }
}
