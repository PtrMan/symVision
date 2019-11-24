package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class NegationNode extends TensorNode {
	public NegationNode(String name, TensorNode child) {
		super(child.shape, name, child);
	}

	public NegationNode(TensorNode child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "NegationNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(TensorNode)
	 */
	@Override
	public Tensor apply(Eval e) {
		Tensor in = children[0].apply(e);
		Tensor out = new Tensor(new float[in.length], in.shape);
		for (int i = 0; i < out.length; i++) {
			out.set(i, -in.get(i));
		}
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		children[0].createGradients(deltas, new NegationNode(name + "_Gradient", parentDelta));
	}
}
