package cg4j.node.nn;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class ReluNode extends TensorNode {
	private float[] vals;

	public ReluNode(String name, TensorNode child) {
		super(child.shape, name, child);
	}

	public ReluNode(TensorNode child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "ReluNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(TensorNode)
	 */
	@Override
	public Tensor apply(Eval e) {
		Tensor in = children[0].apply(e);

		vals = new float[in.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = in.get(i) < 0 ? 0 : in.get(i);
		}
		return new Tensor(vals, in.shape);
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		children[0].createGradients(deltas, new ReluDeltaNode(parentDelta));
	}

	private class ReluDeltaNode extends TensorNode {
		public ReluDeltaNode(TensorNode child) {
			super(child.shape, null, child);
		}

		@Override
		protected String getNodeClassName() {
			return "ReluDeltaNode";
		}

		@Override
		public Tensor apply(Eval e) {
			Tensor in = children[0].apply(e);

			Tensor out = new Tensor(new float[in.length], in.shape);
			for (int i = 0; i < out.length; i++) {
				out.set(i, vals[i] == 0 ? 0 : in.get(i));
			}
			return out;
		}

		@Override
		public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {

		}
	}
}
