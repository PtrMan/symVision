package cg4j.node.nn.conv;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.VariableNode;

import java.util.HashMap;

public class Convolution2DNode extends Node {

	/**
	 * Creates a convolution node from all parameters.
	 *
	 * @param name         The name of the node.
	 * @param child        The child must be a 4-dimensional tensor with shape [Batch, Height, Width, Channels]
	 * @param kernel       The kernel. Normally will be a VariableNode. Must have shape [Height, Width, InChannels, OutChannels].<br>
	 * @param paddingMatch Whether the padding matches the child.<br>
	 *                     If true, the output will have the same size.<br>
	 *                     If false, the output will have the size of
	 *                     <pre>
	 *                                         {@literal
	 *
	 *                                         }
	 *                                         </pre>
	 * @param strides
	 * @param dilation
	 * @see VariableNode
	 */
	public Convolution2DNode(String name, Node child, Node kernel, boolean paddingMatch, int[] strides, int[] dilation) {
		super(ComputeShape(child, kernel, paddingMatch, strides, dilation), null, child, kernel);
	}

	private static int[] ComputeShape(Node child, Node kernel, boolean paddingMatch, int[] strides, int[] dilation) {
//        if (child.shape.length != 4) {
//        }
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	protected String getNodeClassName() {
		return "Convolution2DNode";
	}

	@Override
	public Tensor evaluate(Eval e) {
		return null;
	}

	@Override
	public void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta) {
	}
}
