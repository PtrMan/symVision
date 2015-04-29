package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;

/**
 *
 */
public class Convolution2d
{
    public static IMap2d<Float> convolution(IMap2d<Float> input, IMap2d<Float> inputKernel)
    {
        float[][] inputAsMatrix, kernelAsMatrix;
        IMap2d<Float> resultMap;
        int x, y;
        int i;

        inputAsMatrix = convertMapToArray(input);
        kernelAsMatrix = convertMapToArray(inputKernel);

        resultMap = new Map2d<>(input.getWidth(), input.getLength());

        // TODO optimize for cpu? (opencl is senseless)
        for( i = 0; i < input.getWidth()*input.getLength(); i++ )
        {
            resultMap.setAt(i % input.getWidth(), i / input.getWidth(), 0.0f);
        }

        for( y = 0; y < input.getLength() - inputKernel.getLength(); y++ )
        {
            for( x = 0; x < input.getWidth() - inputKernel.getWidth(); x++ )
            {
                float value;

                value = convolutionAt(inputAsMatrix, kernelAsMatrix, x, y);
                resultMap.setAt(x, y, value);
            }
        }

        return resultMap;
    }

    private static float convolutionAt(float[][] input, float[][] kernel, int startX, int startY)
    {
        float result;
        int x, y;

        result = 0.0f;

        for( y = 0; y < kernel.length; y++ )
        {
            for( x = 0; x < kernel[0].length; x++ )
            {
                result += (input[x + startX][ y + startY] * kernel[x][y]);
            }
        }

        return result;
    }

    private static float[][] convertMapToArray(IMap2d<Float> map)
    {
        float[][] result;
        int x, y;

        result = new float[map.getWidth()][map.getLength()];

        for( y = 0; y < map.getLength(); y++ )
        {
            result[y] = new float[map.getWidth()];
        }

        for( y = 0; y < map.getLength(); y++ )
        {
            for( x = 0; x < map.getWidth(); x++ )
            {
                result[x][y] = map.readAt(x, y);
            }
        }

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
