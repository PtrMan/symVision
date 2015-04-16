package misc;

import Datastructures.IMap2d;
import Datastructures.Map2d;

public class GaussianBlur
{
    public static IMap2d<Float> blur(int radius, IMap2d<Float> input)
    {
        IMap2d<Float> resultMap;
        IMap2d<Float> tempMap;
        float[] kernel;
        int i;
        int di;

        // create kernel
        kernel = new float[(radius - 1) * 2 + 1];
        float variance;
        float normalisation;
        variance = (float)Math.sqrt(0.15f);
        kernel[radius - 1] = 1.0f;
        normalisation = misc.Gaussian.calculateGaussianDistribution(0.0f, 0.0f, variance);
        
        for (di = 1;di < radius;di++)
        {
            float gaussianResult;
            float normalizedResult;

            gaussianResult = misc.Gaussian.calculateGaussianDistribution((float)(di) / (float)(radius), 0.0f, variance);
            normalizedResult = gaussianResult / normalisation;
            kernel[radius - 1 + di] = normalizedResult;
            kernel[radius - 1 - di] = normalizedResult;
        }
        
        resultMap = new Map2d<>(input.getWidth(), input.getLength());
        tempMap = new Map2d<>(input.getWidth(), input.getLength());
        blurX(input, tempMap, kernel);
        blurY(tempMap, resultMap, kernel);
        return resultMap;
    }

    public static void blurX(IMap2d<Float> input, IMap2d<Float> output, float[] kernel)
    {
        int x, y;
        int radius = 1 + (kernel.length - 1) / 2;
        for (y = 0;y < input.getLength();y++)
        {
            for (x = 0;x < input.getWidth();x++)
            {
                int ir;
                float temp;
                temp = 0.0f;
                for (ir = -radius;ir < radius - 1;ir++)
                {
                    if (ir + x < 0 || ir + x >= input.getWidth())
                    {
                        continue;
                    }
                     
                    temp += (input.readAt(x + ir, y) * kernel[radius + ir]);
                }
                output.setAt(x, y, temp);
            }
        }
    }

    public static void blurY(IMap2d<Float> input, IMap2d<Float> output, float[] kernel)
    {
        int x, y;
        int radius = 1 + (kernel.length - 1) / 2;
        for (x = 0;x < input.getWidth();x++)
        {
            for (y = 0;y < input.getLength();y++)
            {
                int ir;
                float temp;

                temp = 0.0f;
                for (ir = -radius;ir < radius - 1;ir++)
                {
                    if (ir + y < 0 || ir + y >= input.getLength())
                    {
                        continue;
                    }
                     
                    temp += (input.readAt(x, y + ir) * kernel[radius + ir]);
                }
                output.setAt(x, y, temp);
            }
        }
    }

}


