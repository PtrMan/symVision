package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.ConstantNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class SquareNode extends Node {
	public SquareNode(String name, Node child) {
		super(child.shape, name, child);
	}

	public SquareNode(Node child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "SquareNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
		Tensor in = children[0].eval(e);
		Tensor out = new Tensor(new float[in.length], in.shape);
		for (int i = 0; i < out.length; i++) {
			out.set(i, in.get(i) * in.get(i));
		}
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, Node> deltas, Node parentDelta) {
		MultiplicationNode mult;
		mult = parentDelta == null ? new MultiplicationNode(
			children[0],
			new ConstantNode(
				new Tensor(
					new float[]{2f}
					,
					1)
			)
		) : new MultiplicationNode(
			parentDelta,
			children[0],
			new ConstantNode(
				new Tensor(
					new float[]{2f}
					,
					1)
			)
		);
		children[0].createGradients(deltas, mult);
	}
}
