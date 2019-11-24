package cg4j.node.io;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.math.AdditionNode;

import java.util.Map;

/**
 * A variable node creates a value that can be modified to minimize/maximize a function
 */
public class VariableNode extends TensorNode {
	public final Tensor val;
	private boolean gradientCreated = false;

	public VariableNode(Tensor val) {
		super(val.shape, null);
		this.val = val;
	}

	public VariableNode(Tensor val, String name) {
		super(val.shape, name);
		this.val = val;
	}

	@Override
	protected String getNodeClassName() {
		return "VariableNode";
	}

	/**
	 * Use {@code Eval#evaluate(Node)}
	 *
	 * @see Eval#evaluate(TensorNode)
	 */
	@Override
	public Tensor apply(Eval e) {
		return val;
	}

	/**
	 * Creates the gradients.
	 *  @param deltas      The deltas of all variables.
	 * @param parentDelta Last node's delta.
	 */
	@Override
	public void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta) {
		if (gradientCreated) {
			((VariableDeltaNode) deltas.get(this)).addChild(parentDelta);
		} else {
			deltas.put(this, new VariableDeltaNode(parentDelta));
			gradientCreated = true;
		}
	}

	private static class VariableDeltaNode extends AdditionNode {
		public VariableDeltaNode(TensorNode... children) {
			super(children);
		}

		public void addChild(TensorNode child) {
			TensorNode[] childrenU = new TensorNode[children.length + 1];
			System.arraycopy(children, 0, childrenU, 0, children.length);
			childrenU[children.length] = child;
			this.children = childrenU;
		}

		@Override
		protected String getNodeClassName() {
			return "VariableDeltaNode";
		}

	}

}
