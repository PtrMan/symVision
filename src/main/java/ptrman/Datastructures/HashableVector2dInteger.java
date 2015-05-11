package ptrman.Datastructures;

import static ptrman.misc.BitOperations.rotateLeft;

/**
 *
 */
public class HashableVector2dInteger extends Vector2d<Integer> {
    public static HashableVector2dInteger fromVector2dInteger(final Vector2d<Integer> argument) {
        return new HashableVector2dInteger(argument.x, argument.y);
    }

    public HashableVector2dInteger(final int x, final int y) {
        super(x, y);
    }

    @Override
    public int hashCode() {
        return rotateLeft(x, 5) ^ y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HashableVector2dInteger))
            return false;
        if (obj == this)
            return true;

        final HashableVector2dInteger rhs = (HashableVector2dInteger)obj;

        return x == rhs.x && y == rhs.y;
    }
}
