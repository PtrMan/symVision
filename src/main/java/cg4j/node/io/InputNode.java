package cg4j.node.io;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;

import java.util.Map;

/**
 * An input node feeds data into the graph.
 *
 * @author nathanwood1
 * @see Node
 * @since 1.0
 */
public class InputNode extends Node {

	/**
	 * Creates an {@code InputNode} from a shape.
	 *
	 * @param shape The shape of the output.
	 * @see Node
	 * @since 1.0
	 */
	public InputNode(int... shape) {
		super(shape, null);
	}

	/**
	 * Creates an {@code InputNode} from a name and shape.
	 *
	 * @param shape The shape of the output.
	 * @param name  {@code String} name of node. Can be null.
	 * @see Node
	 * @since 1.0
	 */
	public InputNode(String name, int... shape) {
		super(shape, name);
	}

	@Override
	protected String getNodeClassName() {
		return "InputNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
		return e.val.get(name);
	}

	@Override
	public void createGradients(Map<VariableNode, Node> deltas, Node parentDelta) {
	}
}
