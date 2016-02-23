package net.sourceforge.jannealer;
import java.util.ArrayList;
/**
 *  An ArrayList of Simplex Objects
 *
 * @see org.theblueplanet.annealing.Simplex
 *
 * @author     Charles Mégnin
 * @since    October 29, 2001
 */
class SimplexList
{
	private ArrayList list;
	private final static double MAXVALUE = -1.0e100;
	/**
	 *  Constructor for the SimplexList object
	 *
	 * @param  itol  Description of Parameter
	 */
	public SimplexList(int itol)
	{
		this.list = new ArrayList(itol);
	}
	/**
	 *  Gets the Index the Simplex with the maximum value field in the
	 *  SimplexList
	 *
	 * @return    The index of the Simplex with the highest value field
	 */
	private int getIndexOfMax()
	{
		double max = MAXVALUE;
		int index = -1;
		for (int ii = 0; ii < list.size(); ii++)
		{
			if (((Simplex) list.get(ii)).getValue() > max)
			{
				max = ((Simplex) list.get(ii)).getValue();
				index = ii;
			}
		}
		return index;
	}
	/**
	 *  Gets the Index the Simplex with the minimum value field in the
	 *  SimplexList
	 *
	 * @return    The index of the Simplex with the lowest value field
	 */
	public Simplex getMinSimplex()
	{
		double min = -MAXVALUE;
		Simplex minSimplex = null;
		for (int ii = 0; ii < list.size(); ii++)
		{
			if (((Simplex) list.get(ii)).getValue() < min)
			{
				minSimplex = (Simplex) list.get(ii);
				min = ((Simplex) list.get(ii)).getValue();
			}
		}
		return minSimplex;
	}
	/**
	 *  Add a Simplex object to the ArrayList
	 *
	 * @param  simplex  Description of Parameter
	 */
	public void add(Simplex simplex)
	{
		list.add(simplex);
	}
	/**
	 *  Computes the difference between the maximum and the minimum values of
	 *  the elements of the SimplexList, <b>excluding the last Simplex added</b>
	 *
	 * @return    The spread
	 */
	public double spread()
	{
		double max = MAXVALUE;
		double min = -max;
		for (int ii = 0; ii < list.size() - 1; ii++)
		{
			if (((Simplex) list.get(ii)).getValue() > max)
			{
				max = ((Simplex) list.get(ii)).getValue();
			}
			if (((Simplex) list.get(ii)).getValue() < min)
			{
				min = ((Simplex) list.get(ii)).getValue();
			}
		}
		return max - min;
	}
	/**
	 *  Returns the size of the ArrayList
	 *
	 * @return    The number of Simplex objects in the SimplexList
	 */
	public int size()
	{
		return list.size();
	}
	/**
	 *  Removes a Simplex from the SimplexList
	 *
	 * @param  ii  The index of the Simplex to be removed
	 */
	protected  void remove(int ii)
	{
		list.remove(ii);
	}

	/**
	 *  Remove from list. 
	*/	
	public void pruneMax()
	{
		remove(getIndexOfMax());
	}
}
