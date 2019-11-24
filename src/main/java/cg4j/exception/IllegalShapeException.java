package cg4j.exception;

import java.util.Arrays;

/**
 * This exception represents a malformed shape.
 *
 * @author nathanwood1
 * @since 1.0
 */
public class IllegalShapeException extends IllegalArgumentException {

	@Deprecated public IllegalShapeException(String str) {
		super(str);
	}

	public IllegalShapeException(int[] a, int[] b) {
		this("", a, b);
	}

	public IllegalShapeException(String str, int[] x) {
		this(str + "\t" + Arrays.toString(x));
	}

	public IllegalShapeException(String str, int[] a, int[] b) {
		this(str + "\t" + Arrays.toString(a)
			+ ", "
			+ Arrays.toString(b)
			+ ")");
	}
}
