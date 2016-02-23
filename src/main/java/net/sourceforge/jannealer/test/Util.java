package net.sourceforge.jannealer.test;

import net.sourceforge.jannealer.AnnealingScheme;

/**
 *
 * Test classes utilities
 * 
 * @author Øyvind Harboe
 */
public class Util
{
	/** dump solution to screen */
	static public void printSolution(AnnealingScheme scheme)
	{
		double[] sol;
		sol = scheme.getSolution();
		System.out.println("Solution:");
		for (int i = 0; i < sol.length; i++)
		{
			System.out.println(sol[i]);
		}
	}
}
