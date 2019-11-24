package cg4j.node.io;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;

import java.util.Map;

/**
 * This class represents a node that always has the same value.
 *
 * @author nathanwood1
 * @see Node
 * @since 1.0
 */
public class ConstantNode extends Node {
	private final Tensor val;

	/**
	 * Create a node from a name and a value.
	 *
	 * @param name
	 * @param val
	 * @see Tensor
	 */
	public ConstantNode(String name, Tensor val) {
		super(val.shape, name);
		this.val = val;
	}

	/**
	 * Create a node from a value.
	 *
	 * @param val
	 * @see Tensor
	 */
	public ConstantNode(Tensor val) {
		super(val.shape, null);
		this.val = val;
	}

	@Override
	protected String getNodeClassName() {
		return "ConstantNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
		return val;
	}

	/**
	 * Creates the gradients.
	 * <pre>
	 * {@literal
	 * f(x) = a
	 *
	 * ∂f/∂x = ∂/∂x[a]
	 *       = 0
	 *
	 * 0 * parentDelta = 0
	 * }
	 * </pre>
	 * The output is zero, so we do nothing.
	 *  @param deltas      The deltas of all variables.
	 * @param parentDelta Last node's delta.
	 */
	@Override
	public void createGradients(Map<VariableNode, Node> deltas, Node parentDelta) {
	}

	@Override
	public String toString() {
		return val.arrayToString();
	}
}
