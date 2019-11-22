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

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 *
 */
public enum AngleHelper
{
	;

	public static double getMinimalAngleInDegreeBetweenNormalizedVectors(final ArrayRealVector a, final ArrayRealVector b)
    {
        final var dotResult = a.dotProduct(b);
        final var angleInRad = Math.acos(dotResult);
        var angleInDegree = angleInRad * (360.0f/(2.0f*(float)Math.PI));
        
        if( angleInDegree > 90.0f )
            angleInDegree = 180.0f - angleInDegree;
        
        return angleInDegree;
    }
}
