/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.misc;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;

public enum GaussianBlur
{
	;

	public static IMap2d<Float> blur(int radius, IMap2d<Float> input)
    {
		int i;
        int di;

        // create kernel
		float[] kernel = new float[(radius - 1) * 2 + 1];
		float variance = (float) Math.sqrt(0.15f);
        kernel[radius - 1] = 1.0f;
		float normalisation = Gaussian.calculateGaussianDistribution(0.0f, 0.0f, variance);
        
        for (di = 1;di < radius;di++)
        {

			float gaussianResult = Gaussian.calculateGaussianDistribution((float) (di) / (float) (radius), 0.0f, variance);
			float normalizedResult = gaussianResult / normalisation;
            kernel[radius - 1 + di] = normalizedResult;
            kernel[radius - 1 - di] = normalizedResult;
        }

		IMap2d<Float> resultMap = new Map2d<>(input.getWidth(), input.getLength());
		IMap2d<Float> tempMap = new Map2d<>(input.getWidth(), input.getLength());
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
				float temp = 0.0f;
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

				float temp = 0.0f;
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


