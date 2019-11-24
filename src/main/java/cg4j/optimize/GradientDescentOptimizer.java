package cg4j.optimize;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.NoVariableNodeToMinimizeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GradientDescentOptimizer extends Optimizer {

	private final java.util.List<VariableNode> control = new ArrayList();

	public float learningRate = 0.001f;
	private Node toChange = null;
	private boolean minimizing;

	public GradientDescentOptimizer() {
	}

	public GradientDescentOptimizer control(VariableNode... toTweak) {
		Collections.addAll(control, toTweak);
		return this;
	}

	@Override
	public void minimize(Node toChange, Map<VariableNode, Node> deltas) {
		this.toChange = toChange;
		this.minimizing = true;
		this.deltas = deltas;
	}

	@Override
	public void maximize(Node toChange, Map<VariableNode, Node> deltas) {
		this.toChange = toChange;
		this.minimizing = false;
		this.deltas = deltas;
	}

	@Override
	public void run(Eval eval) {
		if (toChange == null) {
			throw new NoVariableNodeToMinimizeException("No nodes where told to minimize!");
		}

		boolean min = this.minimizing;
		float learn = this.learningRate;

		for (VariableNode variable : control) {
			Tensor dx = deltas.get(variable).eval(eval);
			for (int i = 0; i < dx.length; i++) {
				Tensor x = variable.val;
				int ii = i % x.length;
				x.set(ii, x.get(ii) + ((min ? -1 : +1) * dx.get(i)) * learn);
			}
		}

	}
}
