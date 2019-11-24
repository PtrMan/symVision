package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class ReciprocalNode extends TensorNode {
	public ReciprocalNode(String name, TensorNode child) {
		super(child.shape, name, child);
	}

	public ReciprocalNode(TensorNode child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "ReciprocalNode";
	}

	@Override
	public Tensor apply(Eval e) {
		Tensor in = children[0].apply(e);
		Tensor out = new Tensor(new float[in.length], in.shape);
		for (int i = 0; i < out.length; i++) {
			out.set(i, 1 / in.get(i));
		}
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		TensorNode delta = new NegationNode(new ReciprocalNode(new SquareNode(parentDelta)));
		children[0].createGradients(deltas, delta);
	}
}
