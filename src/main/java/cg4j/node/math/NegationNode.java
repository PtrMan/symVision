package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.HashMap;

public class NegationNode extends Node {
	public NegationNode(String name, Node child) {
		super(child.shape, name, child);
	}

	public NegationNode(Node child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "NegationNode";
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
			out.setVal(i, -in.get(i));
		}
		return out;
	}

	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
		children[0].createGradients(deltas, new NegationNode(name + "_Gradient", parentDelta));
	}
}
