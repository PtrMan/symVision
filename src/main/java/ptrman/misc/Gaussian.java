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

public enum Gaussian
{
	;

	public static float calculateGaussianDistribution(final float x, final float mu, final float delta)
    {
        final var ONEDIVSQRTTWO = 0.707107f; // 1.0 / Sqrt(2.0)

		final var distToMean = x - mu;
        return ONEDIVSQRTTWO * delta * gaussianExponentTerm(distToMean,delta);
    }

    // http://de.wikipedia.org/wiki/David_Marr
    /**
     * 
     * also known as the "Mexican hat" function
     * 
     */
    public static float calculateMarrWavelet(final float x, final float y, final float delta)
    {

		final var distToMean = x * x + y * y;
		final var factorA = -1.0f / ((float) Math.PI * delta * delta * delta * delta);
		final var factorB = 1.0f - distToMean / (2.0f * delta * delta);
		final var gaussianTerm = gaussianExponentTerm(distToMean, delta);
        return factorA * factorB * gaussianTerm;
    }

    /**
     * 
     * has extra function because it is used for other stuff
     * 
     */
    public static float gaussianExponentTerm(final float distToMean, final float delta)
    {
        return (float)java.lang.Math.exp(-0.5f * ((distToMean * distToMean) / (delta * delta)));
    }

}


