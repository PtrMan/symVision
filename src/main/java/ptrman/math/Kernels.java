/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.math;

import ptrman.Datastructures.Vector2d;

/**
 *
 */
public enum Kernels
{
	;

	public interface IKernel<Type, CoordinateType extends Number>
    {
        Type calculateAt(Vector2d<CoordinateType> position);
    }

    public static class MarrHildrethOperator implements  IKernel<Float, Float>
    {

        public MarrHildrethOperator(float sigma)
        {
            this.sigma = sigma;
        }

        @Override
        public Float calculateAt(Vector2d<Float> position)
        {
			final double factor = -1.0/(java.lang.Math.PI*java.lang.Math.pow(sigma, 4.0f));

			double gaussianFactor = Math.exp(-1.0f * (Maths.squaredDistance(new double[]{position.x, position.y}) / (2.0 * Maths.power2(sigma))));
			double scalingFactor = 1.0 - Maths.squaredDistance(new double[]{position.x, position.y}) / (2.0 * Maths.power2(sigma));

            return (float)(factor*gaussianFactor*scalingFactor);
        }

        private final float sigma;
    }

    /**
     * Marr Hildreth Operator
     * Mexican Hat Operator
     *
     * candidate for SUPERCOMPILATION, slow when executed
     *
     */
    public static float calcMarrHildrethOperator(Vector2d<Float> position, float sigma)
    {
        return new MarrHildrethOperator(sigma).calculateAt(position);
    }
}
