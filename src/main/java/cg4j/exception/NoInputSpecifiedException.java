package cg4j.exception;

/**
 * This exception represents inputs being required but not specified.
 * An example would be:
 * <pre>
 * {@code
 *
 * InputNode a = new InputNode(new int[]{1});
 * Node out = new ReluNode(a);
 *
 * Eval e = new Eval();
 * e.evaluate(out);
 * }
 * </pre>
 *
 * @author nathanwood1
 * @see com.nathanwood1.cg4j.node.io.InputNode
 * @since 1.0
 */
public class NoInputSpecifiedException extends IllegalArgumentException {

	public NoInputSpecifiedException(String str) {
		super(str);
	}
}
