package cg4j.node.tensor;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.HashMap;

public class MeanNode extends Node {
	final int[][] lastShape;
	final int[] lastLength;
	float N = 0;

	public MeanNode(String name, Node... children) {
		super(new int[]{1}, name, children);
		lastShape = new int[children.length][];
		lastLength = new int[children.length];
	}

	public MeanNode(Node... children) {
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
	 * @see Eval#evaluate(Node)
	 */
	@Override
	public Tensor evaluate(Eval e) {
		N = 0;
		float mean = 0;
		for (int i = 0; i < children.length; i++) {
            Tensor in = children[i].eval(e);
			N += in.length;
			for (int j = 0; j < in.length; j++) {
				mean += in.get(j);
			}
			lastShape[i] = in.shape;
			lastLength[i] = in.length;
		}
		N = 1f / N;
		mean *= N;
		return new Tensor(new float[]{mean}, shape);
	}

	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
		for (Node child : children) {
			child.createGradients(deltas, parentDelta);
		}
	}
}
