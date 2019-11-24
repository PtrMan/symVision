package cg4j.node.nn;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.HashMap;

public class ReluNode extends Node {
	private float[] vals;

	public ReluNode(String name, Node child) {
		super(child.shape, name, child);
	}

	public ReluNode(Node child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "ReluNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
        Tensor in = children[0].eval(e);

		vals = new float[in.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = in.get(i) < 0 ? 0 : in.get(i);
		}
		return new Tensor(vals, in.shape);
	}

	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
		children[0].createGradients(deltas, new ReluDeltaNode(parentDelta));
	}

	private class ReluDeltaNode extends Node {
		public ReluDeltaNode(Node child) {
			super(child.shape, null, child);
		}

		@Override
		protected String getNodeClassName() {
			return "ReluDeltaNode";
		}

		@Override
		public Tensor evaluate(Eval e) {
            Tensor in = children[0].eval(e);

			Tensor out = new Tensor(new float[in.length], in.shape);
			for (int i = 0; i < out.length; i++) {
				out.setVal(i, vals[i] == 0 ? 0 : in.get(i));
			}
			return out;
		}

		@Override
		public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {

		}
	}
}
