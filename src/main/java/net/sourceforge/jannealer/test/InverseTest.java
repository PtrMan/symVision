package net.sourceforge.jannealer.test;

import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;

/**
 *
 * Use simulated annealing to calculate the inverse of 
 * two functions with three variables
 * 
 * a=sin(x)*exp(y)*z
 * b=exp(x)*sin(y)
 * 
 * @author �yvind Harboe
 */
public enum InverseTest
{
	;

	public static void main(String[] args)
	{
		AnnealingScheme scheme = new AnnealingScheme();
		
		ObjectiveFunction fn=new InverseObjectiveFunction();
		
		scheme.setFunction(fn);
		
		// figured out these values using ConfigureAnnealing
		scheme.setCoolingRate(83.60192276753233);
		scheme.setTemperature(3.251588722925053);
		scheme.setIterations(46);
		
		System.out.println("Starting search");
		
		/* figure out where the difference is at a minimum */
		scheme.anneal();
		
		Util.printSolution(scheme);
		
		// print out "distance" to solution to build a bit of confidence in the result
		System.out.println("Distance " + fn.distance(scheme.getSolution()));
	}
}
