package cg4j.optimize;

import cg4j.Eval;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.HashMap;
import java.util.Map;

public abstract class Optimizer {
	public Map<VariableNode, Node> deltas;

	public abstract void minimize(Node node, Map<VariableNode, Node> deltas);

	public abstract void maximize(Node node, Map<VariableNode, Node> deltas);

	public abstract void run(Eval eval);

	public void allowOptimize(VariableNode node) {
		deltas.putIfAbsent(node, null);
	}

	public void put(VariableNode node, Node nodeDelta) {
		deltas.put(node, nodeDelta);
	}

	public Optimizer minimize(Node cost) {
		Map<VariableNode, Node> deltas = new UnifiedMap<>();
		cost.createGradients(deltas, null);
		minimize(cost, deltas);
		return this;
	}
}
