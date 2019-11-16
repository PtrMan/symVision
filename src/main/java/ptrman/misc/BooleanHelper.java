package ptrman.misc;

/**
 *
 */
public enum BooleanHelper {
	;

	public static boolean[] booleanNot(final boolean[] array) {
        boolean[] result = new boolean[array.length];

        for( int i = 0; i < result.length; i++ ) {
            result[i] = !array[i];
        }

        return result;
    }

    public static boolean[] booleanAnd(final boolean[] a, final boolean[] b) {
        boolean[] result = new boolean[a.length];

        for( int i = 0; i < result.length; i++ ) {
            result[i] = a[i] && b[i];
        }

        return result;
    }
}
