package net.sourceforge.jannealer.test;
import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;

/** shows how to invoke the annealing code using an example from JSimul */
public class TestAnneal
{
	public static void main(String[] args)
	{
		AnnealingScheme scheme = new AnnealingScheme();
		scheme.setFunction(new ObjectiveFunction()
		{
			private final static int NDIM = 4;
			private final static double RAD = 0.3;
			private final static double AUG = 2.0;
			public int getNdim()
			{
				return NDIM;
			}
			public double distance(double[] vertex)
			{
				double sumd = 0;
				double sumr = 0;
				double[] wid = { 0.0, 1.0, 3.0, 10.0, 30.0 };
				for (int jj = 0; jj < NDIM; jj++)
				{
					double q = vertex[jj] * wid[jj];
					double r =
						(double) (q >= 0 ? (int) (q + 0.5) : - (int) (0.5 - q));
					sumr += q * q;
					sumd += (q - r) * (q - r);
				}
				return 1
					+ sumr
						* (1
							+ (sumd > RAD * RAD
								? AUG
								: AUG * sumd / (RAD * RAD)));
			}
		});
		
		
		scheme.anneal();
		
		Util.printSolution(scheme);
	}
}
