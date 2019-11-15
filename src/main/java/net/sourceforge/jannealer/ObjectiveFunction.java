package net.sourceforge.jannealer;
/**
 *  This interface must be implemented by all the classes that encapsulate
 *  a function that is to be minimized 
 *
 * @author     Charles M?gnin
 * @author     Øyvind Harboe
 * @since    October 29, 2001
 */
public interface ObjectiveFunction
{
	/**
	 *  Returns the number of dimensions for the objective function implemented
	 *
	 * @return    The Ndim value
	 */
	public int getNdim();
	/**
	 * @param params arguments to distance function 
	 * @return
	 * 
	 * Calculates distance. The tolerance/spread refers to the return value 
	 * from this function.
	 */
	public double distance(double[] params);
}
