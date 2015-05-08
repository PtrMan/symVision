package ptrman.Datastructures;

import ptrman.misc.Assert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 */
public class FastBooleanMap2d implements IMap2d<Boolean> {
    public FastBooleanMap2d(int width, int length) {
        Assert.Assert((width % 64) == 0, "width is not divisable by 64 (for 64 bit)");

        this.width = width;
        this.length = length;
        widthDivBy64 = width / 64;

        array = new long[widthDivBy64 * length];
    }

    @Override
    public Boolean readAt(int x, int y) {
        final long nativeValueAtPosition = readLongAtInt(x, y);

        return (nativeValueAtPosition & (1 << (x % 64))) != 0;
    }

    @Override
    public void setAt(int x, int y, Boolean value) {
        final int indexX = x / 64;

        final long mask = 1 << (x % 64);
        if( value ) {
            array[indexX + y * widthDivBy64] |= mask;
        }
        else {
            array[indexX + y * widthDivBy64] &= (mask ^ 0XffffffffffffffffL);
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
        return position.x >= 0 && position.x < width && position.y >= 0 && position.y < length;
    }

    @Override
    public Map2d<Boolean> copy() {
        throw new NotImplementedException();
    }

    public int readByteAtInt(final int x, final int y) {
        final long longAt = readLongAtInt(x, y);

        return (int)((longAt >> (x / 8)) & 0xff);
    }

    public long readLongAtInt(final int x, final int y) {
        final int indexX = x / 64;
        return array[indexX + y * widthDivBy64];
    }

    // datastructure for native 64 bit machines
    private long[] array;

    private final int width;
    private final int widthDivBy64;
    private final int length;
}
