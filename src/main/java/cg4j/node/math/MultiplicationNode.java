package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.*;

public class MultiplicationNode extends Node {


	public MultiplicationNode(String name, Node... children) {
		super(children[0].shape, name, children);
		for (int i = 1; i < children.length; i++) {
			if (!Arrays.equals(new int[]{1}, children[i].shape))
				if (!Node.ShapeEndCompatible(children[0].shape, 0, children[i].shape, 0)) {
					throw new IllegalShapeException(
						"Cannot multiply shapes ("
							+ Arrays.toString(children[0].shape)
							+ ", "
							+ Arrays.toString(children[i].shape)
							+ ")"
					);
				}
		}
	}

	public MultiplicationNode(Node... children) {
		super(children[0].shape, null, children);

		for (int i = 1; i < children.length; i++) {
			if (!Arrays.equals(new int[]{1}, children[i].shape))
				if (!Node.ShapeEndCompatible(children[0].shape, 0, children[i].shape, 0)) {
					throw new IllegalShapeException(
						"Cannot multiply shapes ("
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
		return "MultiplicationNode";
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
			}
			for (int i = 0; i < out.length; i++) {
				if (init) {
					out.setVal(i, out.get(i) * in.get(i % in.length));
				} else {
					out.setVal(i, in.get(i % in.length));
				}
			}
			init = true;
		}
		return out;
	}

	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
		List<Node> multToAdd = new ArrayList<>(children.length - 1);
		for (Node child : children) {
			for (Node childJ : children)
				if (child != childJ)
					multToAdd.add(childJ);
			multToAdd.add(parentDelta);
			child.createGradients(deltas, new MultiplicationNode(multToAdd.toArray(EmptyNodeArray)));
			multToAdd.clear();
		}
	}
}
