package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.Arrays;
import java.util.HashMap;

public class DivisionNode extends Node {
	public DivisionNode(String name, Node a, Node b) {
		super(a.shape, name, a, b);
		if (!Node.ShapeEndCompatible(a.shape, 0, b.shape, 0)) {
			throw new IllegalShapeException(
				"Ends of the shapes must be equal ("
					+ Arrays.toString(a.shape)
					+ ", "
					+ Arrays.toString(b.shape)
					+ ")"
			);
		}
	}

	public DivisionNode(Node a, Node b) {
		super(a.shape, null, a, b);
		if (!Node.ShapeEndCompatible(a.shape, 0, b.shape, 0)) {
			throw new IllegalShapeException(
				"Ends of the shapes must be equal ("
					+ Arrays.toString(a.shape)
					+ ", "
					+ Arrays.toString(b.shape)
					+ ")"
			);
		}
	}

	@Override
	protected String getNodeClassName() {
		return "SubtractionNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
        Tensor aIn = children[0].eval(e);
        Tensor bIn = children[1].eval(e);
		Tensor out = new Tensor(new float[aIn.length], aIn.shape);
		for (int i = 0; i < out.length; i++) {
			out.setVal(i, aIn.get(i) / bIn.get(i));
		}
		return out;
	}

	/**
	 * Creates the gradients.
	 * <pre>
	 * {@literal
	 * f(x, y) = x / y
	 *
	 * ∂f/∂x = ∂/∂x[x / y]
	 *       = ∂/∂x[x] / y
	 *       = 1 / y
	 *
	 * (1 / y) * parentDelta = parentDelta / y
	 *
	 * ∂f/∂y = ∂/∂y[x / y]
	 *       = x * ∂/∂y[1 / y]
	 *       = x * - 1 / y²
	 *       = -x/y²
	 *
	 * -x/y² * parentDelta = -xparentDelta/y²
	 * }
	 * </pre>
	 *
	 * @param deltas      The deltas of all variables.
	 * @param parentDelta Last node's delta.
	 */
	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
		children[0].createGradients(deltas, new DivisionNode(parentDelta, children[1]));
		children[1].createGradients(deltas, new NegationNode(
			new DivisionNode(
				new MultiplicationNode(children[0], parentDelta),
				new SquareNode(children[1])
			)
		));
	}
}
