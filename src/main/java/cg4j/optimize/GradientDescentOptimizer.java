package cg4j.optimize;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.NoVariableNodeToMinimizeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.ArrayList;
import java.util.HashMap;

public class GradientDescentOptimizer extends Optimizer {
	private final java.util.List<VariableNode> control;
	public float learningRate = 0.001f;
	private Node toChange = null;
	private boolean minimizing;

	public GradientDescentOptimizer() {
		control = new ArrayList();
	}

	public GradientDescentOptimizer control(VariableNode... toTweak) {
		for (VariableNode v : toTweak)
			control.add(v);
		return this;
	}

	@Override
	public void minimize(Node toChange, HashMap<VariableNode, Node> deltas) {
		this.toChange = toChange;
		minimizing = true;
		this.deltas = deltas;
	}

	@Override
	public void maximize(Node toChange, HashMap<VariableNode, Node> deltas) {
		this.toChange = toChange;
		minimizing = false;
		this.deltas = deltas;
	}

	@Override
	public void run(Eval eval) {
		if (toChange == null) {
			throw new NoVariableNodeToMinimizeException("No nodes where told to minimize!");
		}

		for (VariableNode variable : control) {
			Node delta = deltas.get(variable);
			Tensor variableD = delta.eval(eval);
			boolean minimizing = this.minimizing;
			for (int i = 0; i < variableD.length; i++) {
				Tensor v = variable.val;
				v.setVal(i % v.length,
					v.get(i % v.length)
						+ ((minimizing ? -1 : +1) * variableD.get(i))
						* learningRate
				);
			}
		}

	}
}
