package cg4j.optimize;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.NoVariableNodeToMinimizeException;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Collections;
import java.util.Map;

public class GradientDescentOptimizer extends Optimizer {


	public float learningRate = 0.001f;
	private TensorNode toChange = null;
	private boolean minimizing;

	public GradientDescentOptimizer() {
	}

	public GradientDescentOptimizer control(VariableNode... controlled) {
		Collections.addAll(control, controlled);
		return this;
	}

	@Override
	public void minimize(TensorNode toChange, Map<VariableNode, TensorNode> deltas) {
		this.toChange = toChange;
		this.minimizing = true;
		this.deltas = deltas;
	}

	@Override
	public void maximize(TensorNode toChange, Map<VariableNode, TensorNode> deltas) {
		this.toChange = toChange;
		this.minimizing = false;
		this.deltas = deltas;
	}

	@Override public Void apply(Eval eval) {

		if (toChange == null) {
			throw new NoVariableNodeToMinimizeException("No nodes where told to minimize!");
		}

		boolean min = this.minimizing;
		double learn = this.learningRate;

		for (VariableNode variable : control) {
			Tensor dx = deltas.get(variable).apply(eval);
			for (int i = 0; i < dx.length; i++) {
				Tensor x = variable.val;
				int ii = i % x.length;
				x.set(ii, (float) (x.get(ii) + ((min ? -1 : +1) * dx.get(i)) * learn));
			}
		}

		return null;
	}
}
