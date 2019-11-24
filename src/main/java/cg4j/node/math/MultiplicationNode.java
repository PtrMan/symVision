package cg4j.node.math;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.*;

public class MultiplicationNode extends TensorNode {


	public MultiplicationNode(String name, TensorNode... children) {
		super(children[0].shape, name, children);
		for (int i = 1; i < children.length; i++)
			if (!Arrays.equals(new int[]{1}, children[i].shape))
				if (!TensorNode.ShapeEndCompatible(children[0].shape, 0, children[i].shape, 0))
					throw new IllegalShapeException("Cannot multiply shapes", children[0].shape, children[1].shape);
	}

	public MultiplicationNode(TensorNode... children) {
		super(children[0].shape, null, children);

		for (int i = 1; i < children.length; i++) {
			if (!Arrays.equals(new int[]{1}, children[i].shape))
				if (!TensorNode.ShapeEndCompatible(children[0].shape, 0, children[i].shape, 0))
					throw new IllegalShapeException("Cannot multiply shapes", children[0].shape, children[1].shape);
		}
	}

	@Override
	protected String getNodeClassName() {
		return "MultiplicationNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(TensorNode)
	 */
	@Override
	public Tensor apply(Eval e) {
		if (children.length == 1) {
			return children[0].apply(e);
		}
		Tensor out = null;

		for (TensorNode child : children) {
			Tensor in = child.apply(e);
			int I = in.length;

			boolean init = out == null;
			if (init) out = new Tensor(new float[I], in.shape);

			for (int i = 0; i < out.length; i++) {
				int ii = i % I;
				out.set(i, init ? in.get(ii) : out.get(i) * in.get(ii));
			}

		}
		return out;
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		List<TensorNode> multToAdd = new ArrayList<>(children.length - 1);
		for (TensorNode child : children) {
			for (TensorNode childJ : children)
				if (child != childJ)
					multToAdd.add(childJ);
			multToAdd.add(parentDelta);
			child.createGradients(deltas, new MultiplicationNode(multToAdd.toArray(EmptyNodeArray)));
			multToAdd.clear();
		}
	}
}
