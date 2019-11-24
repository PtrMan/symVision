package cg4j.optimize;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.NoVariableNodeToMinimizeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AdamOptimizer extends Optimizer {
	public final LinkedList<VariableNode> toTweak;
	public final float beta1 = 0.9f;
	public final float beta2 = 0.999f;
	public final float epsilon = 10E-8f;
	private final HashMap<VariableNode, Tensor> momentM;
	private final HashMap<VariableNode, Tensor> momentV;
	public Node toChange = null;
	public boolean minimizing;
	public float learningRate = 0.001f;
	private float beta1PowerT = Float.NaN;
	private float beta2PowerT;

	public AdamOptimizer() {
		momentM = new HashMap<>();
		momentV = new HashMap<>();

		toTweak = new LinkedList<>();
	}

	public AdamOptimizer control(VariableNode... vv) {
		for (VariableNode v : vv)
			this.toTweak.add(v);
		return this;
	}

	@Override
	public void minimize(Node toMinimize, Map<VariableNode, Node> deltas) {
		this.toChange = toMinimize;
		minimizing = true;
		this.deltas = deltas;

		beta1PowerT = beta1;
		beta2PowerT = beta2;

		for (VariableNode var : toTweak) {
			momentM.put(var, new Tensor(new float[var.val.length], var.val.shape));
			momentV.put(var, new Tensor(new float[var.val.length], var.val.shape));
		}
	}

	@Override
	public void maximize(Node node, Map<VariableNode, Node> deltas) {
		minimizing = false;
		this.deltas = deltas;

		beta1PowerT = beta1;
		beta2PowerT = beta2;

		for (VariableNode var : toTweak) {
			momentM.put(var, new Tensor(new float[var.val.length], var.val.shape));
			momentV.put(var, new Tensor(new float[var.val.length], var.val.shape));
		}
	}

	@Override
	public void run(Eval eval) {
		if (toChange == null) {
			throw new NoVariableNodeToMinimizeException("No nodes where told to minimize!");
		}
		if (minimizing) {
			for (VariableNode var : toTweak) {
				Tensor theta = var.val;

				Node deltaNode = deltas.get(var);
				Tensor delta = deltaNode.eval(eval);

				Tensor mLast = momentM.get(var);
				Tensor vLast = momentV.get(var);

				for (int i = 0; i < var.val.length; i++) {
					float m = beta1 * mLast.get(i) + (1 - beta1) * delta.get(i);
					float v = beta2 * vLast.get(i) + (1 - beta2) * delta.get(i) * delta.get(i);

					float alphaT = (learningRate * (float) Math.sqrt(1 - beta2PowerT)) / (1 - beta1PowerT);
					theta.set(i, theta.get(i) - (alphaT * m) / ((float) Math.sqrt(v) + epsilon));
				}
			}
		} else {
			for (VariableNode var : toTweak) {
				Tensor theta = var.val;

				Node deltaNode = deltas.get(var);
				Tensor delta = deltaNode.eval(eval);

				Tensor mLast = momentM.get(var);
				Tensor vLast = momentV.get(var);

				for (int i = 0; i < var.val.length; i++) {
					float m = beta1 * mLast.get(i) + (1 - beta1) * delta.get(i);
					float v = beta2 * vLast.get(i) + (1 - beta2) * delta.get(i) * delta.get(i);

					float alphaT = (learningRate * (float) Math.sqrt(1 - beta2PowerT)) / (1 - beta1PowerT);
					theta.set(i, theta.get(i) + (alphaT * m) / ((float) Math.sqrt(v) + epsilon));
				}
			}
		}
		beta1PowerT *= beta1PowerT;
		beta2PowerT *= beta2PowerT;
	}
}
