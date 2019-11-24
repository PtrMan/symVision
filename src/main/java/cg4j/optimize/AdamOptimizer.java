package cg4j.optimize;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.NoVariableNodeToMinimizeException;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.HashMap;
import java.util.Map;

public class AdamOptimizer extends Optimizer {
	public final float beta1 = 0.9f;
	public final float beta2 = 0.999f;
	public final float epsilon = 10E-8f;
	private final HashMap<VariableNode, Tensor> momentM;
	private final HashMap<VariableNode, Tensor> momentV;
	public TensorNode toChange = null;
	public boolean minimizing;
	public float learningRate = 0.001f;
	private float beta1PowerT = Float.NaN;
	private float beta2PowerT;

	public AdamOptimizer() {
		momentM = new HashMap<>();
		momentV = new HashMap<>();
	}

	public AdamOptimizer control(VariableNode... vv) {
		for (VariableNode v : vv)
			this.control.add(v);
		return this;
	}

	@Override
	public void minimize(TensorNode toMinimize, Map<VariableNode, TensorNode> deltas) {
		this.toChange = toMinimize;
		minimizing = true;
		this.deltas = deltas;

		beta1PowerT = beta1;
		beta2PowerT = beta2;

		for (VariableNode var : control) {
			momentM.put(var, new Tensor(new float[var.val.length], var.val.shape));
			momentV.put(var, new Tensor(new float[var.val.length], var.val.shape));
		}
	}

	@Override
	public void maximize(TensorNode node, Map<VariableNode, TensorNode> deltas) {
		minimizing = false;
		this.deltas = deltas;

		beta1PowerT = beta1;
		beta2PowerT = beta2;

		for (VariableNode var : control) {
			momentM.put(var, new Tensor(new float[var.val.length], var.val.shape));
			momentV.put(var, new Tensor(new float[var.val.length], var.val.shape));
		}
	}

	@Override public Void apply(Eval eval) {
		if (toChange == null) {
			throw new NoVariableNodeToMinimizeException("No nodes where told to minimize!");
		}

		float alphaT = (minimizing ? -1 : +1) * ((learningRate * (float) Math.sqrt(1 - beta2PowerT)) / (1 - beta1PowerT));

		float beta1 = this.beta1, beta2 = this.beta2;
		float epsilon = this.epsilon;
		for (VariableNode x : control) {
			Tensor theta = x.val;

			Tensor dx = deltas.get(x).apply(eval);

			Tensor mLast = momentM.get(x), vLast = momentV.get(x);

			for (int i = 0; i < x.val.length; i++) {
				double dxi = dx.get(i);
				double m = beta1 * mLast.get(i) + (1 - beta1) * dxi;
				double v = beta2 * vLast.get(i) + (1 - beta2) * dxi * dxi;

				theta.set(i, (float)(theta.get(i) + alphaT * m / (Math.sqrt(v) + epsilon)));
			}
		}
		beta1PowerT *= beta1PowerT;
		beta2PowerT *= beta2PowerT;
		return null;
	}
}
