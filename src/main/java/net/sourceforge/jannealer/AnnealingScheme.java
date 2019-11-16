package net.sourceforge.jannealer;
/**
 * Implements downhill simplex simulated annealing algorithm
 *
 * @author     Charles M�gnin
 * @author     �yvind Harboe
 * @since    October 17, 2001
 */
public class AnnealingScheme
{
	protected double temperature; // Log10 value
	protected double coolingRate; 
	protected int nIterations; 
	protected double[] offset;
	protected double tolerance;
	protected ObjectiveFunction function;

	/** default temperature */
	static public final double DEFAULTTEMP=1.e+6;
	static public final double DEFAULTCOOLINGRATE=20;
	static public final int DEFAULTITERATIONS=20;
	static public final double DEFAULTOLERANCE=1.e-5;

	public AnnealingScheme()
	{
		this.temperature = DEFAULTTEMP;
		this.coolingRate = DEFAULTCOOLINGRATE;
		this.nIterations = DEFAULTITERATIONS;
		this.tolerance = DEFAULTOLERANCE;
	}
	/**
	 * Sets the Temperature attribute of the AnnealingScheme object
	 *
	 * @param  temperature  The new Temperature value
	 */
	public void setTemperature(double temperature)
	{
		this.temperature = temperature;
	}
	/**
	 *  Sets the Tolerance attribute of the AnnealingScheme object. The annealing stops 
	 * once the return values from the ObjectiveFunction varies by less than the tolerance. 
	 *
	 * @param  tolerance  The new Tolerance value
	 */
	public void setTolerance(double tolerance)
	{
		this.tolerance = tolerance;
	}
	/**
	 * Sets a starting point for searching for a solution
	 *
	 * @param  offset  The new Offset value. To reduce API tripwires, 
	 * the offset is duplicated, i.e. the caller can safely modify 
	 * the passed in offset it afterwards.
	 */
	public void setSolution(double[] offset)
	{
		this.offset = offset.clone();
	}
	/**
	 *  Sets the CoolingRate attribute of the AnnealingScheme object
	 *
	 * @param  coolingRate    The new CoolingRate value
	 */
	public void setCoolingRate(double coolingRate)
	{
		this.coolingRate = coolingRate;
	}
	/**
	 *  
	 * Number of iterations before cooling is applied
	 *
	 * @param  nIterations  The new NIterations value
	 */
	public void setIterations(int nIterations)
	{
		this.nIterations = nIterations;
	}
	/**
	 * @return    list of currently best solution. This is a duplicate array that
	 * the caller "owns"
	 */
	public double[] getSolution()
	{
		return offset.clone();
	}
	/**
	 * @return ObjectiveFunction object
	 */
	protected ObjectiveFunction getFunction()
	{
		return function;
	}
	/**
	 * @param function
	 */
	public void setFunction(ObjectiveFunction function)
	{
		this.function = function;
		offset = new double[function.getNdim()];
	}
	/** 
	 * runs an annealing, call getOffset() to get result
	 *  
	 **/
	public void anneal()
	{
		double ybb = 1.0e+30;
		double temptr = temperature;
		int iiter = nIterations;
		
		Schedule schedule = new Schedule(this);
		SimplexList simplexList = new SimplexList(ITOL);
		Simplex simplex;
		schedule.initializeP();
		int loopLevel = -1;
		for (;;)
		{
			loopLevel++;
			schedule.setPDiagonal();
			schedule.setY(getSolution());
			schedule.setYb(1.0e+30);
			int nit = 0;
			for (int jiter = 1; jiter <= 100; jiter++)
			{
				schedule.setIter(iiter);
				//Anneal.amebsa(schedule, scheme.getTolerance(), temptr, idum);
				schedule.amebsa(tolerance, temptr, idum);
				temptr *= (100. - coolingRate) / 100.;
				nit += iiter - schedule.getIter();
				if (schedule.getYb() < ybb)
				{
					ybb = schedule.getYb();
				}
				if (schedule.getIter() > 0)
				{
					break;
				}
			}
			simplex =
				new Simplex(
					schedule.getPb(),
					schedule.getYb(),
					function.getNdim());
			if (simplexList.size() == ITOL)
			{
				simplexList.pruneMax();
			}
			simplexList.add(simplex);
			if (simplexList.size() == ITOL)
			{
				if ((simplexList.spread() < tolerance)
					|| (loopLevel > getMaxIterations()))
				{
					setOffsetFromSimplex(simplexList.getMinSimplex().getVertex());
					break;
				} else
				{
				}
			}
		}
	}
	
	protected void setOffsetFromSimplex(double[] t)
	{
		double[] t2 = new double[t.length - 1];
		System.arraycopy(t, 1, t2, 0, t2.length);
		setSolution(t2);
	}
	
	/** 
	 * finds a solution using downhill simplex.
	 * 
	 * This is work in progress.
	 **/
	protected void simplex()
	{
		double temptr = temperature;
		int iiter = nIterations;
		
		Schedule schedule = new Schedule(this);
		SimplexList simplexList = new SimplexList(ITOL);
		Simplex simplex;
		schedule.initializeP();
		
		schedule.setPDiagonal();
		schedule.setY(getSolution());
		schedule.setYb(1.0e+30);
		int nit = 0;
		for (int jiter = 1; jiter <= 100; jiter++)
		{
			schedule.setIter(iiter);
			schedule.amebsa(tolerance, temptr, idum);
			temptr *= (100. - coolingRate) / 100.;
			nit += iiter - schedule.getIter();
			if (schedule.getIter() > 0)
			{
				break;
			}
		}

		simplex =
			new Simplex(
				schedule.getPb(),
				schedule.getYb(),
				function.getNdim());
		simplexList.add(simplex);
		setOffsetFromSimplex(simplexList.getMinSimplex().getVertex());
	}
	
	/**
	 * @return
	 * 
	 * Override to set maximum number of iterations
	 */
	protected int getMaxIterations()
	{
		return Integer.MAX_VALUE;
	}
	private final static int ITOL = 10;
	private final static long idum = -64;

}
