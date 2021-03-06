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
import ptrman.Datastructures.Vector2d;
import ptrman.math.Kernels;

/**
 *
 */
public enum Convolution2dHelper
{
	;

	public static IMap2d<Float> calculateMarrHildrethOperator(Vector2d<Integer> size, float sigma)
    {
        int x, y;

        IMap2d<Float> kernel = new Map2d<>(size.x, size.y);

        for( y = 0; y < size.y; y++ )
        {
            for( x = 0; x < size.x; x++ )
            {

                float fx = (float) x * 2.0f - 1.0f;
                float fy = (float) y * 2.0f - 1.0f;

                float value = Kernels.calcMarrHildrethOperator(new Vector2d<>(fx, fy), sigma);
                kernel.setAt(x, y, value);
            }
        }

        return kernel;
    }

    /**
     *
     * \param phi angle in radiants
     * \param spartialRatioAspect ellipticity of the support of the Gabor function
     */
    public static IMap2d<Float> calcGaborKernel(int width, float phi, float lambda, float phaseOffset, float spartialRatioAspect)
    {
        float xTick, yTick;
        float sigma;
        int xInt, yInt;

        // constant from http://bmia.bmt.tue.nl/Education/Courses/FEV/course/pdf/Petkov_Gabor_functions2011.pdf
        sigma = 0.56f * lambda;

        IMap2d<Float> resultMap = new Map2d<>(width, width);

        for( yInt = 0; yInt < width; yInt++ ) {
            for( xInt = 0; xInt < width; xInt++ ) {
                float x, y;
                float insideExp, insideCos;
                float filterValue;

                x = ((float)(xInt - width / 2)/(float)width) * 2.0f;
                y = ((float)(yInt - width / 2)/(float)width) * 2.0f;

                xTick = x * (float)Math.cos(phi) + y * (float)Math.sin(phi);
                yTick = -x * (float)Math.sin(phi) + y * (float)Math.cos(phi);

                insideExp = - (xTick*xTick + spartialRatioAspect*spartialRatioAspect * yTick*yTick)/(2.0f*sigma*sigma);
                insideCos = 2.0f*(float)Math.PI * (xTick/lambda) + phaseOffset;

                filterValue = (float)Math.exp(insideExp) * (float)Math.cos(insideCos);

                resultMap.setAt(xInt, yInt, filterValue);
            }
        }

        return resultMap;
    }
}
