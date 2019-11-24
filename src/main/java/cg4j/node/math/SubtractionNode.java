package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class SubtractionNode extends TensorNode {
	public SubtractionNode(String name, TensorNode a, TensorNode b) {
		super(a.shape, name, a, b);
		if (!TensorNode.ShapeEndCompatible(a.shape, 0, b.shape, 0)) {
			throw new IllegalShapeException("Ends of the shapes must be equal", a.shape, b.shape);
		}
	}

	public SubtractionNode(TensorNode a, TensorNode b) {
		super(a.shape, null, a, b);
		if (!TensorNode.ShapeEndCompatible(a.shape, 0, b.shape, 0)) {
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
	 * @see Eval#evaluate(TensorNode)
	 */
	@Override
	public Tensor apply(Eval e) {
		Tensor aIn = children[0].apply(e);
		Tensor bIn = children[1].apply(e);
		Tensor out = new Tensor(new float[aIn.length], aIn.shape);
		for (int i = 0; i < out.length; i++)
			out.set(i, aIn.get(i) - bIn.get(i));
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		children[0].createGradients(deltas, parentDelta);
		children[1].createGradients(deltas, new NegationNode(name + "_Gradient", parentDelta));
	}
}
