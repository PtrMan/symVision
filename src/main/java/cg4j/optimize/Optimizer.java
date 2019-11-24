package cg4j.optimize;

import cg4j.Eval;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.HashMap;

public abstract class Optimizer {
	public java.util.Map<VariableNode, Node> deltas;

	public abstract void minimize(Node node, HashMap<VariableNode, Node> deltas);

	public abstract void maximize(Node node, HashMap<VariableNode, Node> deltas);

	public abstract void run(Eval eval);

	public void allowOptimize(VariableNode node) {
		deltas.putIfAbsent(node, null);
	}

	public void put(VariableNode node, Node nodeDelta) {
		deltas.put(node, nodeDelta);
	}

	public Optimizer minimize(Node cost) {
		HashMap<VariableNode, Node> deltas = new HashMap<>();
		cost.createGradients(deltas, null);
		minimize(cost, deltas);
		return this;
	}
}
