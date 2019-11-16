package net.sourceforge.jannealer.test;

import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;

/** solve (x-3)^2=0 using simulated annealing
 * @author �yvind Harboe
 */
public enum Trivial
{
	;

	public static void main(String[] args)
	{
		AnnealingScheme scheme = new AnnealingScheme();
		
		scheme.setFunction(new ObjectiveFunction()
		{
			public int getNdim()
			{
				return 1;
			}
			/* this function must return a number which is a measure of 
			 * the goodness of the proposed solution
			 */  
			public double distance(double[] vertex)
			{
				return Math.pow(vertex[0]-3, 2);
			}
		});
		
		/* this is where we search for a solution */
		scheme.anneal();
		
		Util.printSolution(scheme);
	}
}
