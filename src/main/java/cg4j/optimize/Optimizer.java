package cg4j.optimize;

import cg4j.Eval;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;
import cg4j.node.math.SquareNode;
import cg4j.node.tensor.MeanNode;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import viralgraph.GraphProcess.GraphNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public abstract class Optimizer extends GraphNode implements Function<Eval,Void> {
	protected Map<VariableNode, TensorNode> deltas;
	protected final java.util.List<VariableNode> control = new ArrayList();
	public TensorNode cost;

	public abstract void minimize(TensorNode node, Map<VariableNode, TensorNode> deltas);

	public abstract void maximize(TensorNode node, Map<VariableNode, TensorNode> deltas);

	public void allowOptimize(VariableNode node) {
		deltas.putIfAbsent(node, null);
	}

	public void put(VariableNode node, TensorNode nodeDelta) {
		deltas.put(node, nodeDelta);
	}

	public Optimizer minimize(TensorNode cost) {
		this.cost = cost;
		Map<VariableNode, TensorNode> deltas = new UnifiedMap<>();
		cost.createGradients(deltas, null);
		minimize(cost, deltas);
		return this;
	}

	public Optimizer minimizeMeanSq(TensorNode yTarget, TensorNode y) {
		return minimize(new MeanNode(new SquareNode(yTarget.minus(y))));
	}

	public double cost(Eval x) {
		return cost.apply(x).get(0);
	}
}
