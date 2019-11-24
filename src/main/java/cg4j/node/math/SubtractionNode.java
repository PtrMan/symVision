package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class SubtractionNode extends Node {
	public SubtractionNode(String name, Node a, Node b) {
		super(a.shape, name, a, b);
		if (!Node.ShapeEndCompatible(a.shape, 0, b.shape, 0)) {
			throw new IllegalShapeException("Ends of the shapes must be equal", a.shape, b.shape);
		}
	}

	public SubtractionNode(Node a, Node b) {
		super(a.shape, null, a, b);
		if (!Node.ShapeEndCompatible(a.shape, 0, b.shape, 0)) {
			throw new IllegalShapeException("Ends of the shapes must be equal", a.shape, b.shape);
		}
	}

	@Override
	protected String getNodeClassName() {
		return "SubtractionNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
        Tensor aIn = children[0].eval(e);
        Tensor bIn = children[1].eval(e);
		Tensor out = new Tensor(new float[aIn.length], aIn.shape);
		for (int i = 0; i < out.length; i++)
			out.set(i, aIn.get(i) - bIn.get(i));
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, Node> deltas, Node parentDelta) {
		children[0].createGradients(deltas, parentDelta);
		children[1].createGradients(deltas, new NegationNode(name + "_Gradient", parentDelta));
	}
}
