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

public enum Map2dConverter {
	;

	public static void booleanToFloat(final IMap2d<Boolean> input, final IMap2d<Float> output) {
        for(var iy = 0; iy < input.getLength(); iy++ )
            for (var ix = 0; ix < input.getWidth(); ix++) {
                final boolean valueAtPosition = input.readAt(ix, iy);
                output.setAt(ix, iy, convertBooleanToFloat(valueAtPosition));
            }
    }

    public static void floatToBoolean(final IMap2d<Float> input, final IMap2d<Boolean> output, final float threshold) {
        for(var iy = 0; iy < input.getLength(); iy++ )
            for (var ix = 0; ix < input.getWidth(); ix++) {
                final float valueAtPosition = input.readAt(ix, iy);
                output.setAt(ix, iy, floatAboveThreshold(valueAtPosition, threshold));
            }
    }

    private static float convertBooleanToFloat(final boolean value) {
        return value ? 1.0f : 0.0f;
    }

    private static boolean floatAboveThreshold(final float value, final float threshold) {
        return value > threshold;
    }
}