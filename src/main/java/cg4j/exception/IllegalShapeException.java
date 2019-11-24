package cg4j.exception;

/**
 * This exception represents a malformed shape.
 *
 * @author nathanwood1
 * @since 1.0
 */
public class IllegalShapeException extends IllegalArgumentException {

	public IllegalShapeException(String str) {
		super(str);
	}
}
