/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;

/**
 *
 */
public enum Convolution2d {
	;

	public static IMap2d<Float> convolution(final IMap2d<Float> input, final IMap2d<Float> inputKernel) {

		final var inputAsMatrix = convertMapToArray(input);
		final var kernelAsMatrix = convertMapToArray(inputKernel);

		final IMap2d<Float> resultMap = new Map2d<>(input.getWidth(), input.getLength());

        // TODO optimize for cpu? (opencl is senseless)
		final var area = input.getWidth() * input.getLength();
		for(int i = 0; i < area; i++ ) resultMap.setAt(i % input.getWidth(), i / input.getWidth(), 0.0f);

		final var H = input.getLength() - inputKernel.getLength();
		final var W = input.getWidth() - inputKernel.getWidth();
		final var wHalf = inputKernel.getWidth() / 2;
		final var hHalf = inputKernel.getLength() / 2;
		for(int y = 0; y < H; y++ )
			for (int x = 0; x < W; x++)
				resultMap.setAt(x + wHalf, y + hHalf, convolutionAt(inputAsMatrix, kernelAsMatrix, x, y));

        return resultMap;
    }

    private static float convolutionAt(final float[][] input, final float[][] kernel, final int startX, final int startY) {

		double result = 0.0f;

		final var w = kernel.length;
		final var h = kernel[0].length;
		for(int y = 0; y < w; y++ )
			for (int x = 0; x < h; x++)
				result += (input[x + startX][y + startY] * kernel[x][y]);

        return (float)result;
    }

    private static float[][] convertMapToArray(final IMap2d<Float> map)
    {
		int y;

		final var W = map.getWidth();
		final var H = map.getLength();
		final var result = new float[W][H];

        for(y = 0; y < W; y++ )
            result[y] = new float[H];

		for(int x = 0; x < W; x++ )
	        for(y = 0; y < H; y++ )
                result[x][y] = map.readAt(x, y);

        return result;
    }


    /*
    uncommented because its FFT code
    private static IMap2d<Float> cutAndConvertToMap(float[][] values, int startX, int startY, int width, int height)
    {
        int x, y;
        IMap2d<Float> result;

        result = new Map2d<Float>(width, height);

        for( y = 0; y < height; y++ )
        {
            for( x = 0; x < width; x++ )
            {
                float value;

                value = values[x + startX][y + startY];
                result.setAt(x, y, value);
            }
        }

        return result;
    }

    private static float[][] getPadded(IMap2d<Float> map, int width, int height)
    {
        float[][] resultArray;
        int x, y;

        int mapWidth, mapHeight;

        mapWidth = map.getWidth();
        mapHeight = map.getLength();

        resultArray = new float[width][height];

        for( y = 0; y < mapHeight; y++ )
        {
            for( x = 0; x < mapWidth; x++ )
            {
                resultArray[x][y] = map.readAt(x, y);
            }
        }

        return resultArray;
    }



    private static fft.ComplexNumber[,] multiplyComplexNumberArray(ref fft.ComplexNumber[,] a, ref fft.ComplexNumber[,] b)
{
    int x, y;
    fft.ComplexNumber[,] result;

    System.Diagnostics.Debug.Assert(a.GetLength(0) == b.GetLength(0));
    System.Diagnostics.Debug.Assert(a.GetLength(1) == b.GetLength(1));

    result = new fft.ComplexNumber[a.GetLength(0), a.GetLength(1)];

    for( y = 0; y < a.GetLength(1); y++ )
    {
        for( x = 0; x < a.GetLength(0); x++ )
        {
            result[x, y] = a[x, y] * b[x, y];
        }
    }

    return result;
}

    private static int calculateCombinedWidthPowerTwo(int a, int b)
    {
        int temp;
        int factor;

        temp = a + b;

        factor = 1;

        for(;;)
        {
            if (factor >=temp)
            {
                return factor;
            }

            // else we are here

            factor *= 2;
        }
    }
    */
}
