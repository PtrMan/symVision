package cg4j.exception;

/**
 * This represents trying to optimize when the optimizer
 * hasn't been told what {@code VariableNode} to optimize.
 *
 * @author nathanwood1
 * @see com.nathanwood1.cg4j.node.io.VariableNode
 * @see com.nathanwood1.cg4j.optimize.Optimizer
 * @since 1.0
 */
public class NoVariableNodeToMinimizeException extends IllegalArgumentException {
	public NoVariableNodeToMinimizeException(String str) {
		super(str);
	}
}
