package ptrman.misc;

/**
 * Created by r0b3 on 10.05.15.
 */
public class BitOperations {
    public static int rotateLeft(final int number, final int bits) {
        return (number << bits) | (number >>> (32 - bits));
    }
}
