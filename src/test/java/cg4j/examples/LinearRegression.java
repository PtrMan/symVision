package cg4j.examples;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.Node;
import cg4j.node.io.InputNode;
import cg4j.node.io.VariableNode;
import cg4j.node.math.AdditionNode;
import cg4j.node.math.MultiplicationNode;
import cg4j.node.math.SquareNode;
import cg4j.node.math.SubtractionNode;
import cg4j.node.tensor.MeanNode;
import cg4j.optimize.GradientDescentOptimizer;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class LinearRegression {
	@Test
	public void linearRegression1() {
		/*
		 * Create the variables.
		 * Variables can be tweaked to find optimal values.
		 * Here, they represent 'y = mx + c'.
		 * We are trying to find the optimal values for our data set.
		 */
		VariableNode m = new VariableNode(new Tensor(new float[1], new int[]{1}));
		VariableNode c = new VariableNode(new Tensor(new float[1], new int[]{1}));

		/*
		 * Create an InputNode.
		 * This node is used to feed data into the graph.
		 * Here, it represents the x in 'y = mx + c'.
		 */
		InputNode x = new InputNode(-1, 1);

		/*
		 * This is where we program in the formula 'y = mx + c'
		 */

		/*
		 * 'yTarget' is the optimal value for 'y'.
		 * We find the mean squared cost through the code below.
		 */
		InputNode yTarget = new InputNode(new int[]{-1, 1});
		Node cost = new MeanNode(new SquareNode(new SubtractionNode(yTarget, new AdditionNode(new MultiplicationNode(x, m), c))));

		/*
		 * Create a GradientDescentOptimizer and allow it to tweak 'm' and 'c'.
		 * We also set the learning rate and what variable to minimize.
		 */
		GradientDescentOptimizer optimizer = new GradientDescentOptimizer().control(m, c);
		optimizer.learningRate = 0.001f;

		/*
		 * Create the deltas and use them to minimize 'cost'.
		 */
		HashMap<VariableNode, Node> deltas = new HashMap<>();
		cost.createGradients(deltas, null);
		optimizer.minimize(cost, deltas);

		Tensor[] vals = {
			new Tensor(new float[]{
				9,
				8,
				6,
				14,
				12,
				10
			}, new int[]{6, 1}),
			new Tensor(new float[]{
				4,
				2,
				1,
				8,
				1,
				7
			}, new int[]{6, 1})
		};

		for (int i = 0; i < 4000; i++) {
			/*
			 * Create an evaluator with input values
			 */
			Eval eval = new Eval()
                    .set(x, vals[0])
                    .set(yTarget, vals[1]);

			/*
			 * Allow the gradient descent algorithm to run.
			 */
			optimizer.run(eval);

			/*
			 * Display the cost every 100 iterations.
			 */
			if (i % 100 == 0) {
				System.out.printf("Error: %f\n", cost.eval(eval).get(0));
			}
		}

		/*
		 * Display the data.
		 */
		Eval eval = new Eval()
			.set(x, vals[0])
			.set(yTarget, vals[1]);
		System.out.println();
		float C = cost.eval(eval).get(0);
		System.out.println("Cost: " + C);
		System.out.println("y = " + m.eval(eval).get(0) + "x + " + c.eval(eval).get(0));
		assertEquals(C, 5.08f, 0.01f);
	}
}
