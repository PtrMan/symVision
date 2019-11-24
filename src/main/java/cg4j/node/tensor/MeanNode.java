package cg4j.node.tensor;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Map;

public class MeanNode extends TensorNode {
	final int[][] lastShape;
	final int[] lastLength;


	public MeanNode(String name, TensorNode... children) {
		super(new int[]{1}, name, children);
		lastShape = new int[children.length][];
		lastLength = new int[children.length];
	}

	public MeanNode(TensorNode... children) {
		super(new int[]{1}, null, children);
		lastShape = new int[children.length][];
		lastLength = new int[children.length];
	}

	@Override
	protected String getNodeClassName() {
		return "MeanNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(TensorNode)
	 */
	@Override
	public Tensor apply(Eval e) {
		int N = 0;
		double mean = 0;
		for (int i = 0; i < children.length; i++) {
			Tensor in = children[i].apply(e);
			N += in.length;
			for (int j = 0; j < in.length; j++) {
				mean += in.get(j);
			}
			lastShape[i] = in.shape;
			lastLength[i] = in.length;
		}
		mean /= N;
		return new Tensor(new float[]{(float) mean}, shape);
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		for (TensorNode child : children)
			child.createGradients(deltas, parentDelta);
	}
}
