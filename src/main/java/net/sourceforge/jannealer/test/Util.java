package net.sourceforge.jannealer.test;

import net.sourceforge.jannealer.AnnealingScheme;

/**
 *
 * Test classes utilities
 * 
 * @author �yvind Harboe
 */
public enum Util
{
	;

	/** dump solution to screen */
	static public void printSolution(AnnealingScheme scheme)
	{
		double[] sol;
		sol = scheme.getSolution();
		System.out.println("Solution:");
		for (double v : sol) {
			System.out.println(v);
		}
	}
}
