package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;

import java.util.Arrays;

/**
 * Created by me on 7/17/15.
 */
public class FloatMap2d implements IMap2d<Float> {
    private final int w;
    private final int h;
    public final float data[][];

    public FloatMap2d(int w, int h) {
        this.w = w;
        this.h = h;
        this.data = new float[h][];
        for (int i = 0; i < h; i++) data[i] = new float[w];
    }

    public FloatMap2d(FloatMap2d f) {
        throw new RuntimeException("not impl yet");
        //this.w = f.w;
        //this.h = f.h;
        //this.data = f.data; //TODO copy 2d array
    }

    @Override
    public void clear() {
        for (float[] d : data)
            Arrays.fill(d, 0);
    }


    @Override
    public Float readAt(int x, int y) {
        return get(x, y);
    }

    public float get(int x, int y) {
        return data[y][x];
    }

    @Override
    public void setAt(int x, int y, Float value) {
        set(x, y, value);
    }

    public void set(int x, int y, float v) {
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
