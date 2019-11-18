package net.sourceforge.jannealer;
/**
 *  This class contains the Annealing scheme. It is mostly a JAVA wrapper
 *  around two functions : amebsa and amotsa in Press, Teukolsky, Vetterling
 *  and Flannery, "Numerical Recipes in C", Cambridge University Press, 2nd
 *  edition, 1994. The original versions of amebsa and amotsa can be found on
 *  pages 452 and 454 respectively.
 *
 * @author     Charles M�gnin
 * @since    November 3, 2001
 */
class Schedule
{
	private final ObjectiveFunction function;
	private int iter;
	private double yb;
	private double[][] p;
	private double[] y;
	private final double[] pb;
	private double[] psum;
	private final int mp;
	private final int np;
	/**
	 *  Constructor for the Schedule object
	 *
	 * @param  scheme                      Description of Parameter
	 * @exception  InstantiationException  Description of Exception
	 * @exception  IllegalAccessException  Description of Exception
	 */
	protected Schedule(AnnealingScheme scheme)
	{
		this.function = scheme.getFunction();
		this.np = function.getNdim();
		this.mp = np + 1;
		pb = new double[np + 1];
	}
	/**
	 *  Sets the Psum attribute of the Schedule object
	 *
	 * @param  mpts  The new Psum value
	 */
	protected void setPsum(int mpts)
	{
		this.psum = new double[np + 1];
		for (int n = 1; n <= np; n++)
		{
			double sum;
			int m;
			for (sum = 0.0, m = 1; m <= mpts; m++)
			{
				sum += p[m][n];
			}
			psum[n] = sum;
		}
	}
	/**
	 *  Sets the Iter attribute of the Schedule object
	 *
	 * @param  iter  The new Iter value
	 */
	protected void setIter(int iter)
	{
		this.iter = iter;
	}
	/**
	 *  Sets the Yb attribute of the Schedule object
	 *
	 * @param  yb  The new Yb value
	 */
	protected void setYb(double yb)
	{
		this.yb = yb;
	}
	/**
	 *  Sets the Y attribute of the Schedule object
	 *
	 * @param  xoff  The new Y value
	 */
	protected void setY(double[] xoff)
	{
		double[] x = new double[np + 1];
		y = new double[mp + 1];
		for (int ii = 1; ii <= mp; ii++)
		{
			for (int jj = 1; jj <= np; jj++)
			{
				x[jj] = (p[ii][jj] += xoff[jj - 1]);
			}
			y[ii] = invokeDist(x);
		}
	}
	/**
	 *  Sets the PDiagonal attribute of the Schedule object
	 */
	protected void setPDiagonal()
	{
		for (int jj = 2; jj <= mp; jj++)
		{
			p[jj][jj - 1] = 1.0;
		}
	}
	/**
	 *  Gets the Np attribute of the Schedule object
	 *
	 * @return    The Np value
	 */
	protected int getNp()
	{
		return np;
	}
	/**
	 *  Gets the Pb attribute of the Schedule object
	 *
	 * @return    The Pb value
	 */
	protected double[] getPb()
	{
		return pb;
	}
	/**
	 *  Gets the Pb attribute of the Schedule object
	 *
	 * @param  ii  Description of Parameter
	 * @return     The Pb value
	 */
	protected double getPb(int ii)
	{
		return pb[ii];
	}
	/**
	 *  Gets the Iter attribute of the Schedule object
	 *
	 * @return    The Iter value
	 */
	protected int getIter()
	{
		return iter;
	}
	/**
	 *  Gets the Yb attribute of the Schedule object
	 *
	 * @return    The Yb value
	 */
	protected double getYb()
	{
		return yb;
	}
	/**
	 *  Gets the Y attribute of the Schedule object
	 *
	 * @return    The Y value
	 */
	protected double[] getY()
	{
		return y;
	}
	/**
	 *  Gets the Y attribute of the Schedule object
	 *
	 * @param  ii  Description of Parameter
	 * @return     The Y value
	 */
	protected double getY(int ii)
	{
		return y[ii];
	}
	/**
	 *  Gets the P attribute of the Schedule object
	 *
	 * @return    The P value
	 */
	protected double[][] getP()
	{
		return p;
	}
	/**
	 *  Gets the P attribute of the Schedule object at requested index
	 *
	 * @param  ii  The i index
	 * @param  jj  The j index
	 * @return     The P[ii][jj] value
	 */
	protected double getP(int ii, int jj)
	{
		return p[ii][jj];
	}
	/**
	 *  Multi-dimensional minimization algorithm.
	 *
	 * @param  ftol    The fractional tolerance
	 * @param  temptr  The temperature
	 * @param  idum    The random number generator seed
	 */
	protected void amebsa(double ftol, double temptr, long idum)
	{
		int i;
		int j;
		int m;
		int n;
		setPsum(mp);
		double tt = -temptr;
		for (;;)
		{
			int ilo = 1;
			int ihi = 2;
			double ylo = fluctuate(1, tt, idum);
			double ynhi = ylo;
			double yhi = fluctuate(2, tt, idum);
			if (ylo > yhi)
			{
				ihi = 1;
				ilo = 2;
				ynhi = yhi;
				yhi = ylo;
				ylo = ynhi;
			}
			for (i = 3; i <= mp; i++)
			{
				double yt = fluctuate(i, tt, idum);
				if (yt <= ylo)
				{
					ilo = i;
					ylo = yt;
				}
				if (yt > yhi)
				{
					ynhi = yhi;
					ihi = i;
					yhi = yt;
				} else if (yt > ynhi)
				{
					ynhi = yt;
				}
			}
			double rtol =
				2.0 * Math.abs(yhi - ylo) / (Math.abs(yhi) + Math.abs(ylo));
			if (rtol < ftol || getIter() < 0)
			{
				swap(ilo);
				break;
			}
			increaseIter(-2);
			double ytry = amotsa(np, ihi, yhi, -1.0, idum, tt);
			if (ytry <= ylo)
			{
				ytry = amotsa(np, ihi, yhi, 2.0, idum, tt);
			} else if (ytry >= ynhi)
			{
				double ysave = yhi;
				ytry = amotsa(np, ihi, yhi, 0.5, idum, tt);
				if (ytry >= ysave)
				{
					contract(mp, ilo);
					increaseIter(-np);
					setPsum(mp);
				}
			} else
			{
				increaseIter(1);
			}
		}
	}
	/**
	 *  Description of the Method
	 */
	protected void initializeP()
	{
		p = new double[mp + 1][np + 1];
		for (int ii = 1; ii <= mp; ii++)
		{
			for (int jj = 0; jj <= np; jj++)
			{
				p[ii][jj] = 0.0;
			}
		}
	}
	/**
	 *  Swapping utility
	 *
	 * @param  ilo  Description of Parameter
	 */
	protected void swap(int ilo)
	{
		double swap = y[1];
		y[1] = y[ilo];
		y[ilo] = swap;
		for (int n = 1; n <= np; n++)
		{
			swap = p[1][n];
			p[1][n] = p[ilo][n];
			p[ilo][n] = swap;
		}
	}
	/**
	 *  Contraction utility
	 *
	 * @param  mpts  Description of Parameter
	 * @param  ilo   Description of Parameter
	 */
	protected void contract(int mpts, int ilo)
	{
		for (int i = 1; i <= mpts; i++)
		{
			if (i != ilo)
			{
				for (int j = 1; j <= np; j++)
				{
					psum[j] = 0.5 * (p[i][j] + p[ilo][j]);
					p[i][j] = psum[j];
				}
				y[i] = invokeDist(psum);
			}
		}
	}
	/**
	 *  Description of the Method
	 *
	 * @param  yIndex  Description of Parameter
	 * @param  tt      Description of Parameter
	 * @param  idum    Description of Parameter
	 * @return         Description of the Returned Value
	 */
	protected double fluctuate(int yIndex, double tt, long idum)
	{
		return y[yIndex] + tt * Math.log(RandomUtil.ran1(idum));
	}
	/**
	 *  Extrapolation through the face of the Simplex
	 *
	 * @param  ndim  Description of Parameter
	 * @param  ihi   Description of Parameter
	 * @param  yhi   Description of Parameter
	 * @param  fac   Description of Parameter
	 * @param  idum  Description of Parameter
	 * @param  tt    Description of Parameter
	 * @return       Description of the Returned Value
	 */
	protected double amotsa(
		int ndim,
		int ihi,
		double yhi,
		double fac,
		long idum,
		double tt)
	{
		double[] ptry = new double[ndim + 1];
		double fac1 = (1.0 - fac) / ndim;
		double fac2 = fac1 - fac;
		double ytry = 0;
		for (int j = 1; j <= ndim; j++)
		{
			ptry[j] = psum[j] * fac1 - p[ihi][j] * fac2;
		}
		ytry = invokeDist(ptry);
		if (ytry <= yb)
		{
			if (ndim >= 0) System.arraycopy(ptry, 1, pb, 1, ndim);
			yb = ytry;
		}
		double yflu = ytry - tt * Math.log(RandomUtil.ran1(idum));
		if (yflu < yhi)
		{
			y[ihi] = ytry;
			yhi = yflu;
			for (int j = 1; j <= ndim; j++)
			{
				psum[j] += ptry[j] - p[ihi][j];
				p[ihi][j] = ptry[j];
			}
		}
		return yflu;
	}
	
	/** the API operates with Java style array offsets, whereas some of the internals
	 * still operate with FORTRAN like offsets.
	 * 
	 * This function converts between the two conventions while we are waiting for the
	 * internals to be shifted to Java style offsets.
	 */
	private double invokeDist(double[] ptry)
	{
		double ytry;
		double[] t = new double[ptry.length - 1];
		System.arraycopy(ptry, 1, t, 0, t.length);
		ytry = function.distance(t);
		return ytry;
	}
	/**
	 *  Increments the attribute iter by dIter
	 *
	 * @param  dIter  The increment
	 */
	protected void increaseIter(int dIter)
	{
		iter += dIter;
	}
}
