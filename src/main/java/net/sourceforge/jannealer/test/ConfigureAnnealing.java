package net.sourceforge.jannealer.test;

import java.util.Date;

import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;

/**
 * Use simulated annealing to find the best configuration of
 * simulated annealing.
 * 
 * @author �yvind Harboe
 *
 */
public enum ConfigureAnnealing
{
	;

	public static void main(String[] args)
	{
		AnnealingScheme configure = new AnnealingScheme();
		
		configure.setFunction(new ObjectiveFunction()
		{
			public int getNdim()
			{
				return 3;
			}

			/* returns amount of time it took to find a solution */
			public double distance(double[] args)
			{
				Date start=new Date();
				AnnealingScheme scheme = new AnnealingScheme();
				
				double coolingRate=args[0];
				double startTemp=args[1];
				int iterations=(int)args[2];

				if ((coolingRate>99)||(coolingRate<1)||(startTemp<=0)||(iterations<1))
				{
					/* this function increases in magnitude as arguments get further out of range */
					double slide=1000;
					if (coolingRate>99)
					{
						slide+=Math.pow(coolingRate, 2);
					}
					if (coolingRate<1)
					{
						slide+=Math.pow(1-coolingRate, 2);
					}
					if (startTemp<=0)
					{
						slide+=Math.pow(startTemp, 2);
					}
					if (iterations<1)
					{
						slide+=Math.pow(1-iterations, 2);
					}
					return slide;
				}
				
				scheme.setCoolingRate(coolingRate);
				scheme.setTemperature(startTemp);
				
				scheme.setFunction(new InverseObjectiveFunction());
				/* figure out where the difference is at a minimum */
				scheme.anneal();
				
				Date stop=new Date();
				
				double time=(double)(stop.getTime()-start.getTime())/1000;
				System.out.println("time " + time + " coolingrate " + coolingRate + " temperature " + startTemp + " iterations " + iterations);
				return time;
			}
			
		});
		configure.setSolution(new double[]
		{
			50, // start at cooling rate 50%
			1e+6, // start hot
			20 // start at 20 iterations before cooling
		});
	
		configure.setTolerance(0.01); // once we are within 100ms of where we want to be, we're ok.
		configure.anneal();
		
		double[]offset=configure.getSolution();
		
		System.out.println("Use coolingrate " + offset[0] + " temperature " + offset[1] + " iterations " + offset[2]);				
	}
}