package cg4j.examples;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.InputNode;
import cg4j.node.io.VariableNode;
import cg4j.node.math.AdditionNode;
import cg4j.node.math.MatrixMultiplicationNode;
import cg4j.node.math.SquareNode;
import cg4j.node.math.SubtractionNode;
import cg4j.node.nn.SigmoidNode;
import cg4j.node.tensor.MeanNode;
import cg4j.optimize.AdamOptimizer;
import cg4j.optimize.Optimizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertTrue;


public class XOR {
	@Test
	public void xor1() {
		/*
		 * Seed the randomness so it runs the same every time
		 */
		Random seed = new Random(0);

		/*
		 * Create the weights and biases
		 */
		VariableNode layer1Weights = new VariableNode(Tensor.fromRandom(seed, -1, 1, new int[]{2, 4}));
		VariableNode layer1Biases = new VariableNode(Tensor.fromRandom(seed, -1, 1, new int[]{4}));
		VariableNode layer2Weights = new VariableNode(Tensor.fromRandom(seed, -1, 1, new int[]{4, 1}));
		VariableNode layer2Biases = new VariableNode(Tensor.fromRandom(seed, -1, 1, new int[]{1}));

		/*
		 * Allows input of data into the graph.
		 */
		InputNode x = new InputNode(-1, 2);
		InputNode yTarget = new InputNode(-1, 1);

		/*
		 * This is the base for all fully-connected layers.
		 */
		Node y;
		y = new MatrixMultiplicationNode(x, layer1Weights);
		y = new SigmoidNode(new AdditionNode(y, layer1Biases));

		y = new MatrixMultiplicationNode(y, layer2Weights);
		y = new SigmoidNode(new AdditionNode(y, layer2Biases));

		/*
		 * Create a target and mean squared error.
		 */
		Node cost = new MeanNode(new SquareNode(new SubtractionNode(yTarget, y)));

		/*
		 * Create an optimizer and allow it to tweak the weights and biases.
		 */
		Optimizer optimizer = new AdamOptimizer()
			.control(layer1Weights, layer1Biases, layer2Weights, layer2Biases)
			.minimize(cost);
		((AdamOptimizer)optimizer).learningRate = 0.1f;

		/*
		 * Create the inputs and target outputs for the network.
		 */
		Tensor xVal = new Tensor(new float[]{
			0, 0,
			0, 1,
			1, 0,
			1, 1
		}, new int[]{4, 2});
		Tensor yTargetVal = new Tensor(new float[]{
			0,
			1,
			1,
			0
		}, new int[]{4, 1});

		/*
		 * Run for 100 iterations.
		 */
		for (int i = 0; i < 100; i++) {
			/*
			 * Feed the data into the optimizer.
			 */
			Eval eval = new Eval()
				.set(x, xVal)
				.set(yTarget, yTargetVal);
			optimizer.run(eval);

			/*
			 * Print the error every 10 iterations.
			 */
			if (i % 10 == 0) {
				System.out.printf("Error: %f\n", cost.eval(eval).get(0));
			}
		}

		System.out.println();

		{
			/*
			 * Show the total error.
			 */
			float err = cost.eval(new Eval()
				.set(x, xVal)
				.set(yTarget, yTargetVal)).get(0);
			System.out.printf("Total Error: %f%n%n", err);
			assertTrue(err < 0.01f);
		}

		/*
		 * Display the network's output.
		 */
		for (float[] vals : new float[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}}) {

			System.out.println(Arrays.toString(vals) + " -> " + y.eval(new Eval()
				.set(x, new Tensor(vals, new int[]{1, 2}))).arrayToString());
		}
	}
}
