package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.Arrays;
import java.util.HashMap;

public class AdditionNode extends Node {
	public AdditionNode(String name, Node... children) {
		super(children[0].shape, name, children);
		for (int i = 1; i < children.length; i++) {
			if (!Node.ShapeEndCompatible(children[0].shape, 0, children[i].shape, 0)) {
				throw new IllegalShapeException(
					"Cannot add shapes ("
						+ Arrays.toString(children[0].shape)
						+ ", "
						+ Arrays.toString(children[i].shape)
						+ ")"
				);
			}
		}
	}

	public AdditionNode(Node... children) {
		super(children[0].shape, null, children);
		for (int i = 1; i < children.length; i++) {
			if (!Node.ShapeEndCompatible(children[0].shape, 0, children[i].shape, 0)) {
				throw new IllegalShapeException(
					"Cannot add shapes ("
						+ Arrays.toString(children[0].shape)
						+ ", "
						+ Arrays.toString(children[i].shape)
						+ ")"
				);
			}
		}
	}

	@Override
	protected String getNodeClassName() {
		return "Addition";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
		if (children.length == 1) {
            return children[0].eval(e);
        }
		Tensor out = null;
		boolean init = false;
		for (Node child : children) {
            Tensor in = child.eval(e);
			if (!init) {
				out = new Tensor(new float[in.length], in.shape);
				for (int i = 0; i < out.length; i++) {
					out.setVal(i, in.get(i % in.length));
				}
			} else {
				for (int i = 0; i < out.length; i++) {
					out.setVal(i, out.get(i) + in.get(i % in.length));
				}
			}
			init = true;
		}
		return out;
	}

	/**
	 * Creates the gradients.
	 * <pre>
	 * {@literal
	 * f(x, y) = x + y
	 *
	 * ∂f/∂x = ∂/∂x[x + y]
	 *       = ∂/∂x[x] + ∂/∂x[y]
	 *       = 1 + 0
	 *       = 1
	 *
	 * 1 * parentDelta = parentDelta
	 * }
	 * </pre>
	 * The output is {@code parentDelta}, so we forward the output to all our children.
	 *
	 * @param deltas      The deltas of all variables.
	 * @param parentDelta Last node's delta.
	 */
	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
		for (Node child : children) {
			child.createGradients(deltas, parentDelta);
		}
	}
}
