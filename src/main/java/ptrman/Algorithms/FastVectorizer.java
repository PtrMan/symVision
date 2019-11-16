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
