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

public enum Map2dStencil
{
	;

	static public void stencilSingleValue(Map2d<Integer> input, IMap2d<Boolean> result, int value)
    {
        int ix, iy;

        for( iy = 0; iy < input.getWidth(); iy++ )
        {
            for( ix = 0; ix < input.getLength(); ix++ )
            {

                int valueAtPosition = input.readAt(ix, iy);
                result.setAt(ix, iy, valueAtPosition == value);
            }
        }
    }

    static public void stencilValues(Map2d<Integer> input, IMap2d<Boolean> result, Iterable<Integer> values, int maxValue)
    {

        boolean[] stencilBooleanArray = convertValuesToBooleanArray(values, maxValue);
        stencilWithBooleanArray(input, result, stencilBooleanArray);
    }

    static private boolean[] convertValuesToBooleanArray(Iterable<Integer> values, int maxValue)
    {

        boolean[] resultArray = new boolean[maxValue + 1];

        for( int iterationValue : values )
        {
            resultArray[iterationValue] = true;
        }

        return resultArray;
    }

    static private void stencilWithBooleanArray(Map2d<Integer> input, IMap2d<Boolean> result, boolean[] booleanArray)
    {
        int ix, iy;

        for( iy = 0; iy < input.getWidth(); iy++ )
        {
            for( ix = 0; ix < input.getLength(); ix++ )
            {

                int valueAtPosition = input.readAt(ix, iy);
                result.setAt(ix, iy, booleanArray[valueAtPosition]);
            }
        }
    }
}
