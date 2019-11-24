package cg4j.examples;

import cg4j.Eval;
import cg4j.Tensor;
import cg4j.node.TensorNode;
import cg4j.node.io.InputNode;
import cg4j.node.io.VariableNode;
import cg4j.node.nn.SigmoidNode;
import cg4j.optimize.AdamOptimizer;
import cg4j.optimize.Optimizer;
import org.junit.Test;
import viralgraph.GraphProcess;

import java.util.Arrays;
import java.util.Random;

import static cg4j.Tensor.variableRandom;
import static org.junit.Assert.assertTrue;


public class XOR {

	InputNode x, yTarget;
	Random rng = new Random(0);
	VariableNode weights1 = variableRandom(rng, -1, 1, new int[]{2, 4});
	VariableNode biases1 = variableRandom(rng, -1, 1, new int[]{4});
	VariableNode weights2 = variableRandom(rng, -1, 1, new int[]{4, 1});
	VariableNode biases2 = variableRandom(rng, -1, 1, new int[]{1});
	TensorNode y;
	Optimizer opt;
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


	@Test public void testEvalPure() {

		x = new InputNode(-1, 2);
		yTarget = new InputNode(-1, 1);

		y = new SigmoidNode(x.times(weights1).plus(biases1));
		y = new SigmoidNode(y.times(weights2).plus(biases2));


		opt = new AdamOptimizer()
			.control(weights1, biases1, weights2, biases2)
			.minimizeMeanSq(yTarget, y);
		((AdamOptimizer)opt).learningRate = 0.1f;

		for (int i = 0; i < 100; i++) {

			Eval xy = new Eval().set(x, xVal).set(yTarget, yTargetVal);

			opt.apply(xy);

			if (i % 10 == 0)
				System.out.printf("Error: %f\n", opt.cost(xy));
		}

		System.out.println();

		{
			/*
			 * Show the total error.
			 */
			float err = (float) opt.cost(new Eval().set(x, xVal).set(yTarget, yTargetVal));
			System.out.printf("Total Error: %f%n%n", err);
			assertTrue(err < 0.01f);
		}

		/*
		 * Display the network's output.
		 */
		for (float[] vals : new float[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}}) {
			System.out.println(Arrays.toString(vals) + " -> " + y.apply(new Eval()
				.set(x, new Tensor(vals, new int[]{1, 2}))).arrayToString());
		}
	}

	@Test public void testEvalGraph() {

		GraphProcess g = new GraphProcess();

		x = g.node(-1, 2);
		yTarget = g.node(-1, 1);

		y = new SigmoidNode(x.times(weights1).plus(biases1));
		y = new SigmoidNode(y.times(weights2).plus(biases2));

		opt = new AdamOptimizer()
			.control(weights1, biases1, weights2, biases2)
			.minimizeMeanSq(yTarget, y);
		((AdamOptimizer)opt).learningRate = 0.1f;

		g.nodeOrAdd(opt.cost);

		for (int i = 0; i < 100; i++) {
			Eval xy = new Eval();
			xy.set(x, xVal);
			xy.set(yTarget, yTargetVal);

			g.set(x, xy);
			g.set(yTarget, xy);

			//Eval xy = new Eval().set(x, xVal).set(yTarget, yTargetVal);

//			opt.accept(xy);
//
			if (i % 10 == 0)
				System.out.printf("Error: %f\n", opt.cost(xy));
		}
		System.out.println(g.flow);
		assertTrue(g.flow.nodes().size() > 10);

	}
}
