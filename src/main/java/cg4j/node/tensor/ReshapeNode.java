package cg4j.node.tensor;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.IllegalShapeException;
import cg4j.node.TensorNode;
import cg4j.node.io.VariableNode;

import java.util.Arrays;
import java.util.Map;

public class ReshapeNode extends TensorNode {
	public ReshapeNode(int[] shape, String name, TensorNode child) {
		super(shape, name, child);
	}

	public ReshapeNode(int[] shape, TensorNode child) {
		super(shape, null, child);
	}

	@Override
	protected String getNodeClassName() {
		return "ReshapeNode";
	}

	@Override
	public Tensor apply(Eval e) {
		Tensor in = children[0].apply(e);

		int[] newShape = new int[shape.length];
		int unknownIndex = -1;
		int unknownLength = in.length;
		for (int i = 0; i < shape.length; i++) {
			if (shape[i] == -1) {
				if (unknownIndex != -1) {
					throw new IllegalShapeException("Cannot reshape multiple unknowns ("
						+ Arrays.toString(shape)
						+ ")"
					);
				}
				unknownIndex = i;
			} else {
				newShape[i] = shape[i];
				unknownLength /= shape[i];
			}
		}
		newShape[unknownIndex] = unknownLength;
		return new Tensor(in.getVals(), newShape);
	}

	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		children[0].createGradients(deltas, new ReshapeNode(children[0].shape, parentDelta));
	}
}
