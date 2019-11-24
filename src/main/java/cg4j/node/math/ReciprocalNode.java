package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class ReciprocalNode extends Node {
	public ReciprocalNode(String name, Node child) {
		super(child.shape, name, child);
	}

	public ReciprocalNode(Node child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "ReciprocalNode";
	}

	@Override
	public Tensor evaluate(Eval e) {
		Tensor in = children[0].eval(e);
		Tensor out = new Tensor(new float[in.length], in.shape);
		for (int i = 0; i < out.length; i++) {
			out.set(i, 1 / in.get(i));
		}
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, Node> deltas, Node parentDelta) {
		Node delta = new NegationNode(new ReciprocalNode(new SquareNode(parentDelta)));
		children[0].createGradients(deltas, delta);
	}
}
