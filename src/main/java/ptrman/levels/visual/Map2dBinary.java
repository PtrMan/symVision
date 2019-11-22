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

import java.util.stream.IntStream;

public enum Map2dBinary {
	;

	public static IMap2d<Boolean> negate(final IMap2d<Boolean> input) {
        final IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for (var y = 0; y < input.getLength(); y++)
            for (var x = 0; x < input.getWidth(); x++) result.setAt(x, y, !input.readAt(x, y));

        return result;
    }

    public static IMap2d<Boolean> corode(final IMap2d<Boolean> input) {
        final IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for(var y = 1; y < input.getLength() - 1; y++ )
            for (var x = 1; x < input.getWidth() - 1; x++) {
                // optimized

                if (
                        !input.readAt(x - 1, y - 1) ||
                                !input.readAt(x, y - 1) ||
                                !input.readAt(x + 1, y - 1) ||

                                !input.readAt(x - 1, y) ||
                                !input.readAt(x, y) ||
                                !input.readAt(x + 1, y) ||

                                !input.readAt(x - 1, y + 1) ||
                                !input.readAt(x, y + 1) ||
                                !input.readAt(x + 1, y + 1)
                ) continue;

                result.setAt(x, y, true);
            }

        return result;
    }

    // edge thinning
    // http://fourier.eng.hmc.edu/e161/lectures/morphology/node2.html
    public static IMap2d<Boolean> edgeThinning(final IMap2d<Boolean> input) {
        final var neightbors = new boolean[8];

        final IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for(var y = 1; y < input.getLength() - 1; y++ )
            for (var x = 1; x < input.getWidth() - 1; x++) {

                neightbors[0] = input.readAt(x - 1, y - 1);
                neightbors[1] = input.readAt(x, y - 1);
                neightbors[2] = input.readAt(x + 1, y - 1);

                neightbors[3] = input.readAt(x + 1, y);
                neightbors[4] = input.readAt(x + 1, y + 1);

                neightbors[5] = input.readAt(x, y + 1);
                neightbors[6] = input.readAt(x - 1, y + 1);

                neightbors[7] = input.readAt(x - 1, y);

                // count zero crossing
                final var zeroCrossing = (int) IntStream.range(0, 8 - 1).filter(i -> neightbors[i] && !neightbors[i + 1]).count();

                // count set pixels
                final var setPixels = (int) IntStream.range(0, 8).filter(i -> neightbors[i]).count();

                final var deletePixel = setPixels == 0 ||
                        setPixels == 1 ||
                        setPixels == 7 ||
                        setPixels == 8 ||
                        zeroCrossing >= 2;

                if (!deletePixel) result.setAt(x, y, true);
            }

        return result;
    }

    // http://fourier.eng.hmc.edu/e161/lectures/morphology/node3.html
    public static IMap2d<Boolean> skeletalize(final IMap2d<Boolean> input) {
        int x, y;

        final IMap2d<Integer> counterMap = new Map2d<>(input.getWidth(), input.getLength());
        final IMap2d<Boolean> output = new Map2d<>(input.getWidth(), input.getLength());

        for( y = 0; y < input.getLength(); y++ )
            for (x = 0; x < input.getWidth(); x++) if (input.readAt(x, y)) counterMap.setAt(x, y, 1);

        var k = 0;

        final var minArray = new int[5];
        while (true) {
            k += 1;

            var changeMade = false;

            for( y = 1; y < input.getLength() - 1; y++ )
                for (x = 1; x < input.getWidth() - 1; x++) {

                    if (counterMap.readAt(x, y) != k) continue;
                    // we are here if it is the case

                    changeMade = true;

                    //minArray[0] = counterMap.readAt(x-1, y-1);
                    minArray[0] = counterMap.readAt(x, y - 1);
                    //minArray[2] = counterMap.readAt(x+1, y-1);

                    minArray[1] = counterMap.readAt(x - 1, y);
                    minArray[2] = counterMap.readAt(x, y);
                    //minArray[2] = counterMap.readAt(x, y);
                    minArray[3] = counterMap.readAt(x + 1, y);

                    //minArray[6] = counterMap.readAt(x-1, y+1);
                    minArray[4] = counterMap.readAt(x, y + 1);
                    //minArray[8] = counterMap.readAt(x+1, y+1);

                    final var min = minOfArray(minArray);
                    final var newValue = min + 1;

                    counterMap.setAt(x, y, newValue);

                    //output.writeAt(x, y, true);
                }

            if( !changeMade ) break;
        }

        final var maxArray = new int[5];
        for (y = 1; y < input.getLength()-1; y++)
            for (x = 1; x < input.getWidth() - 1; x++) {

                final int compare = counterMap.readAt(x, y);

                if (compare == 0) continue;

                maxArray[0] = counterMap.readAt(x + 1, y);
                maxArray[1] = counterMap.readAt(x - 1, y);
                maxArray[2] = counterMap.readAt(x, y + 1);
                maxArray[3] = counterMap.readAt(x, y - 1);
                maxArray[4] = counterMap.readAt(x, y);

                if (compare == maxOfArray(maxArray)) output.setAt(x, y, true);
            }

        return output;
    }

    public static IMap2d<Boolean> threshold(final IMap2d<Float> input, final float threshold) {
        final IMap2d<Boolean> result = new Map2d<>(input.getWidth(), input.getLength());

        for(var y = 0; y < input.getLength(); y++ )
            for (var x = 0; x < input.getWidth(); x++) {
                final var val = input.readAt(x, y) > threshold;
                result.setAt(x, y, val);
            }

        return result;
    }

    public static void orInplace(final IMap2d<Boolean> a, final IMap2d<Boolean> b, final IMap2d<Boolean> result) {
        for(var y = 0; y < a.getLength(); y++ )
            for (var x = 0; x < a.getWidth(); x++) result.setAt(x, y, a.readAt(x, y) || b.readAt(x, y));
    }

    public static void andInplace(final IMap2d<Boolean> a, final IMap2d<Boolean> b, final IMap2d<Boolean> result) {
        for(var y = 0; y < a.getLength(); y++ )
            for (var x = 0; x < a.getWidth(); x++) result.setAt(x, y, a.readAt(x, y) && b.readAt(x, y));
    }

    public static void xorInplace(final IMap2d<Boolean> a, final IMap2d<Boolean> b, final IMap2d<Boolean> result) {
        for(var y = 0; y < a.getLength(); y++ )
            for (var x = 0; x < a.getWidth(); x++) result.setAt(x, y, a.readAt(x, y) ^ b.readAt(x, y));
    }



    private static int minOfArray(final int[] array) {
        var min = array[0];

        for(var i = 1; i < array.length; i++ ) if (array[i] < min) min = array[i];

        return min;
    }

    private static int maxOfArray(final int[] array) {
        var max = array[0];

        for(var i = 1; i < array.length; i++) if (array[i] > max) max = array[i];

        return max;
    }
}
