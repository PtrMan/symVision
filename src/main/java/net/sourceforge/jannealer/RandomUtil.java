package net.sourceforge.jannealer;
/** Returns randomly distributed numbers (white distribution)
  * over a specified interval for 4 specified types:
  * int, long, float & double
  */
enum RandomUtil
{
	;
	private static final long IA = 16807;
	private static final long IM = 2147483647;
	private static final long IQ = 127773;
	private static final long IR = 2836;
	private static final int NTAB = 32;
	private static final long NDIV = 1 + (IM - 1) / NTAB;
	private static final double AM = 1.0 / IM;
	private static final double EPS = 1.2e-7;
	private static final double RNMX = (1.0 - EPS);
	private static long iy = 0;
	private static final long[] iv = new long[NTAB];
	/** Adapted from Numerical Recipes in C
	 *  'Minimal' random number generator
	 *  Call with idum negative to initialize
	 */
	public static double ran1(long idum)
	{
		int j;
		long k;
		double temp;
		if (idum <= 0 || iy != 0)
		{
			idum = -(idum) < 1 ? 1 : -(idum);
			for (j = NTAB + 7; j >= 0; j--)
			{
				k = (idum) / IQ;
				idum = IA * (idum - k * IQ) - IR * k;
				if (idum < 0)
					idum += IM;
				if (j < NTAB)
					iv[j] = idum;
			}
			iy = iv[0];
		}
		k = (idum) / IQ;
		idum = IA * (idum - k * IQ) - IR * k;
		if (idum < 0)
			idum += IM;
		j = (int) (iy / NDIV);
		iy = iv[j];
		iv[j] = idum;
		if ((temp = AM * iy) > RNMX)
			return RNMX;
		return temp;
	}
}
