package cg4j.node.nn;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class SigmoidNode extends TensorNode {
	private float[] vals;

	public SigmoidNode(String name, TensorNode child) {
		super(child.shape, name, child);
	}

	public SigmoidNode(TensorNode child) {
		super(child.shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "SigmoidNode";
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
			vals[i] = 1 / (1 + (float) Math.exp(-in.get(i)));
		}

		return new Tensor(vals, in.shape);
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		children[0].createGradients(deltas, new SigmoidDeltaNode(parentDelta));
	}

	private class SigmoidDeltaNode extends TensorNode {
		public SigmoidDeltaNode(TensorNode child) {
			super(child.shape, null, child);
		}

		@Override
		protected String getNodeClassName() {
			return "SigmoidDeltaNode";
		}

		@Override
		public Tensor apply(Eval e) {
			Tensor in = children[0].apply(e);

			Tensor out = new Tensor(new float[in.length], in.shape);
			for (int i = 0; i < out.length; i++) {
				out.set(i, vals[i] * (1 - vals[i]) * in.get(i));
			}
			return out;
		}

		@Override
		public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		}
	}
}
