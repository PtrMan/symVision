package cg4j;

import cg4j.exception.IllegalShapeException;
import cg4j.node.io.InputNode;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will calculate the graph.
 *
 * @author nathanwood1
 * @since 1.0
 */
public class Eval {
	public final java.util.Map<String, Tensor> val;

	/**
	 * Creates an {@code Eval}
	 *
	 * @since 1.0
	 */
	public Eval() {
		val = new ConcurrentHashMap<>();
	}

	/**
	 * Adds an input to the graph.
	 *
	 * @param node  An {@code InputNode} representing where in the graph the input should be added.
	 * @param input The input to go into the graph.
	 * @return This eval class. This allows {@code new Eval().addInput(...).addInput...}
	 * @throws IllegalShapeException if the data's shape doesn't match the input node's.
	 * @since 1.0
	 */
	public Eval set(InputNode node, Tensor input) {
		validate(node, input);
		val.put(node.name, input);
		return this;
	}


	private static void validate(InputNode node, Tensor input) {
		int[] shape1 = node.shape;
		int[] shape2 = input.shape;

		if (shape1.length != shape2.length) {
			throw new IllegalShapeException(
				"Input data doesn't have the same dimensionality as the input node ("
					+ Arrays.toString(shape1)
					+ ".length != "
					+ Arrays.toString(shape2)
					+ ".length)"
			);
		}
		for (int i = 0; i < shape1.length; i++) {
			if (shape1[i] != -1) { // '-1' means we haven't specified a dimension yet
				if (shape1[i] != shape2[i]) {
					throw new IllegalShapeException(
						"Input data doesn't have the same shape as input node ("
							+ Arrays.toString(shape1)
							+ " != "
							+ Arrays.toString(shape2)
							+ ")"
					);
				}
			}
		}
	}


}
