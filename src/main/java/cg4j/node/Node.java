package cg4j.node;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.exception.NoInputSpecifiedException;
import cg4j.node.io.InputNode;
import cg4j.node.io.VariableNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: ADD JAVADOC FOR ALL NODES
public abstract class Node implements java.io.Serializable {
	public static final Node[] EmptyNodeArray = new Node[0];

	private static final AtomicInteger stringGlobalCounter = new AtomicInteger();

	public final String name;
	public final int[] shape;
	public final int length;

	protected Node[] children;

	public Node(int[] shape, String name, Node... children) {
		this.children = children;
		this.shape = shape;
		this.name = name == null ? getNodeClassName() + "_" + stringGlobalCounter.getAndIncrement() : name;
		int length = 1;
		for (int x : shape) {
			length *= x;
		}
		this.length = length;
	}

	public static boolean ShapeEndCompatible(int[] aShape, int aStart, int[] bShape, int bStart) {
		for (int i = 1; i <= Math.min(aShape.length - bStart, bShape.length - aStart); i++) {
			if (aShape[aShape.length - i - bStart] != bShape[bShape.length - i - aStart]) {
				return false;
			}
		}
		return true;
	}

	protected abstract String getNodeClassName();

	public Node[] getChildren() {
		return children;
	}

	public abstract Tensor evaluate(Eval e);

	public abstract void createGradients(HashMap<VariableNode, Node> deltas, Node parentDelta);

	@Override
	public String toString() {
		return "(" + name + ", " + Arrays.toString(children) + ")";
	}

	public Tensor eval(Eval e) {
//		Tensor y = e.val.get(name);
//		if (y==null) { // If we've already calculated it, just use that value
//			if (this instanceof InputNode) { // If it's an input node, the data should already be there.
//				throw new NoInputSpecifiedException(
//					"No input was specified for "
//						+ this
//				);
//			}
//			y = evaluate(e);
//			if (e.cache(this))
//				e.val.put(this, y);
//		}
		return evaluate(e);
	}
}
