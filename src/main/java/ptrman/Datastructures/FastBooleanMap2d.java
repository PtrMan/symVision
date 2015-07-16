package ptrman.Datastructures;

import ptrman.misc.Assert;

import java.util.Arrays;

/**
 *
 */
public class FastBooleanMap2d implements IMap2d<Boolean> {
    public FastBooleanMap2d(int width, int length) {
        Assert.Assert((width % 64) == 0, "width is not divisable by 64 (for 64 bit)");

        this.width = width;
        this.length = length;


        array = new long[width/64 * length];
    }

    public FastBooleanMap2d(FastBooleanMap2d f) {
        this.width = f.width;
        this.length = f.length;
        this.array = Arrays.copyOf(f.array, f.array.length);
    }

    @Override
    public Boolean readAt(final int x, final int y) {
        //Assert.Assert(inBounds(new Vector2d<>(x, y)), "");

        final long nativeValueAtPosition = readLongAtInt(x, y);

        return (nativeValueAtPosition & (1L << (x % 64))) == (1L << (x % 64));
    }

    @Override
    public void setAt(final int x, final int y, final Boolean value) {
        //Assert.Assert(inBounds(new Vector2d<>(x, y)), "");

        final int indexX = x / 64;

        final long mask = 1L << (x % 64);

        final int t = indexX + y * width/64;

        if( value ) {
            array[t] |= mask;
        }
        else {
            array[t] &= (~mask);
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean inBounds(Vector2d<Integer> position) {
        return position.xInt() >= 0 && position.xInt() < width && position.yInt() >= 0 && position.yInt() < length;
    }

    @Override
    public FastBooleanMap2d copy() {
        return new FastBooleanMap2d(this);
    }

    public int readByteAtInt(final int x, final int y) {
        //Assert.Assert(inBounds(new Vector2d<>(x, y)), "");

        final long longAt = readLongAtInt(x, y);

        return (int)((longAt >>> ((x / 8) % (64/8))) & 0xff);
    }

    public long readLongAtInt(final int x, final int y) {
        //Assert.Assert(inBounds(new Vector2d<>(x, y)), "");

        final int indexX = x / 64;

        /*
        System.out.flush();
        System.out.println("FastBooleanMap2d index");
        System.out.flush();
        System.out.println(indexX + y * widthDivBy64);
        System.out.flush();
        */

        return array[indexX + y * width/64];
    }


    // datastructure for native 64 bit machines
    private final long[] array;

    private final int width;

    private final int length;
}
