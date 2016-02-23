package net.sourceforge.jannealer.test;

import net.sourceforge.jannealer.ObjectiveFunction;


/** Normally this would just be an anonymous class, but we're using it
 * from two classes.
 */
public final class InverseObjectiveFunction implements ObjectiveFunction
{
	InverseObjectiveFunction()
	{
	}
	int count;
	static final double A=2;
	static final double B=3;
	public int getNdim()
	{
		return 3;
	}
	public double distance(double[] arg)
	{
		double error;
		error=Math.pow(A-Math.sin(arg[0])*Math.exp(arg[1])*arg[2], 2);
		error+=Math.pow(B-Math.exp(arg[0])*Math.sin(arg[1]), 2);
		//System.out.println("Error " + count++ + " " + error);
		return error;
	}
}