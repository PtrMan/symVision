/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Algorithms;

import ptrman.Datastructures.IMap2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Vectorizes a bitmap made out of only lines to line primitives
 * * supplies connectivity information
 * * modifies the input bitmap
 *
 * * vectorization input doesn't have any guaranties in case of multi-neighbor pixels
 */
public class FastVectorizer {
    public IMap2d<Boolean> workingBitmap;

    // TODO< datastructures for patches >

    /**
     *
     * @param boundaryMin
     * @param boundaryMax
     */
    /**
     * works by searching the next neighbor in the neightborhood of the cursor and moving to that location
     */
    public void vectorize(int[] boundaryMin, int[] boundaryMax) {

        int[] start = scanForFirstPixel(boundaryMin, boundaryMax);

        Stack<int[]> cursorStack = new Stack<>();
        cursorStack.push(start);

		while (!cursorStack.isEmpty()) {

			int[] cursor = cursorStack.pop();

			for (; ; ) {
				workingBitmap.setAt(cursor[0], cursor[1], false);
				// TODO< save cursor position in currrent patch >

				List<int[]> neightborhoodpixels = getNeightborSetPixelPositions(cursor);

				if (neightborhoodpixels.isEmpty()) {
					continue;
				}

				// carry on with last neightborhoodpixel and push the other pixels on the stack
				final int[] nextCursorPosition = neightborhoodpixels.get(neightborhoodpixels.size() - 1);
				neightborhoodpixels.remove(neightborhoodpixels.size() - 1);
				cursorStack.addAll(neightborhoodpixels);

				cursor = nextCursorPosition;
			}


		}




    }

    // returns null if there is no pixel
    private int[] scanForFirstPixel(int[] boundaryMin, int[] boundaryMax) {
        for( int y = boundaryMin[1]; y < boundaryMax[1]; y++ ) {
            for( int x = boundaryMin[0]; x < boundaryMax[0]; x++ ) {
                if( workingBitmap.readAt(x, y) ) {
                    return new int[]{x, y};
                }
            }
        }

        return null;
    }

    private List<int[]> getNeightborSetPixelPositions(int[] cursorPosition) {
        List<int[]> resultNeightborhoodPixels = new ArrayList<>();

        for( int yDelta = -1; yDelta <= 1; yDelta++ ) {
            for( int xDelta = -1; xDelta <= 1; xDelta++ ) {
                int x = cursorPosition[0] + xDelta;
                int y = cursorPosition[1] + yDelta;

                if( xDelta == 0 && yDelta == 0 ) {
                    continue;
                }

                if( x < 0 || x >= workingBitmap.getWidth() || y < 0 || y >= workingBitmap.getLength() ) {
                    continue;
                }

                if( workingBitmap.readAt(x, y) ) {
                    resultNeightborhoodPixels.add(new int[]{x, y});
                }
            }
        }

        return resultNeightborhoodPixels;
    }
}
