package cg4j.node;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.io.VariableNode;
import cg4j.node.math.AdditionNode;
import cg4j.node.math.MatrixMultiplicationNode;
import cg4j.node.math.SubtractionNode;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

//TODO: ADD JAVADOC FOR ALL NODES
public abstract class TensorNode implements java.io.Serializable, Function<Eval,Tensor> {
	public static final TensorNode[] EmptyNodeArray = new TensorNode[0];

	private static final AtomicInteger stringGlobalCounter = new AtomicInteger();

	public final String name;
	public final int[] shape;
	public final int length;

	protected TensorNode[] children;

	public TensorNode(int[] shape, String name, TensorNode... children) {
		this.children = children;
		this.shape = shape;
		this.name = name == null ? getNodeClassName() + "_" + stringGlobalCounter.getAndIncrement() : name;
		int length = 1;
		for (int x : shape) {
			length *= x;
		}
		this.length = length;
	}

	public void forEachRecurse(Consumer<TensorNode> each) {
		for (TensorNode c : children) {
			each.accept(c);
			c.forEachRecurse(each);
		}
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

	public TensorNode[] getChildren() {
		return children;
	}



	public abstract void createGradients(Map<VariableNode, TensorNode> deltas, TensorNode parentDelta);

	@Override
	public String toString() {
		return "(" + name + ", " + Arrays.toString(children) + ")";
	}

	public AdditionNode plus(TensorNode a) {
		return new AdditionNode(this, a);
	}
	public SubtractionNode minus(TensorNode a) {
		return new SubtractionNode(this, a);
	}

	public MatrixMultiplicationNode times(TensorNode a) {
		return new MatrixMultiplicationNode(this, a);
	}
}
