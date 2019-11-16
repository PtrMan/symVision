package net.sourceforge.jannealer;
/**
 *  Holds the vertices of the simplex and the function value at the vertices
 *
 * @author     Charles M?gnin
 * @since    October 29, 2001
 * @version    1.0
 */
class Simplex
{
	private double[] vertex;
	private double value;
	private int ndim;
	/**
	 *  Constructor for the Simplex object
	 *
	 * @param  vertex  The vertices of the simplex
	 * @param  value   The function value
	 * @param  ndim    The number of dimensions
	 */
	public Simplex(double[] vertex, double value, int ndim)
	{
		this.ndim = ndim;
		this.vertex = new double[ndim + 1];
		System.arraycopy(vertex, 0, this.vertex, 0, ndim + 1);
		this.value = value;
	}
	/**
	 *  Gets the Vertex attribute of the Simplex object
	 *
	 * @return    The Vertex value
	 */
	public double[] getVertex()
	{
		return vertex;
	}
	/**
	 *  Gets the Value attribute of the Simplex object
	 *
	 * @return    The Value value
	 */
	public double getValue()
	{
		return value;
	}
}
