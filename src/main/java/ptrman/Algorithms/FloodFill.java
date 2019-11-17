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
import ptrman.Datastructures.Vector2d;

public enum FloodFill {
	;

	public interface IPixelSetListener {
        void seted(Vector2d<Integer> position);
    }

    private static class SpecificExecutor<Type> implements GeneralizedFloodFill.IFillExecutor {
        public SpecificExecutor(IPixelSetListener pixelSetListener, IMap2d<Type> map, final Type targetColor, final Type replacementColor) {
            this.pixelSetListener = pixelSetListener;
            this.map = map;
            this.targetColor = targetColor;
            this.replacementColor = replacementColor;
        }

        @Override
        public void fillAt(Vector2d<Integer> position, Vector2d<Integer> fromDirection) {
            map.setAt(position.x, position.y, replacementColor);

            pixelSetListener.seted(position);
        }

        @Override
        public boolean canAndShouldBeFilled(Vector2d<Integer> position) {
            if( targetColor.equals(replacementColor) ) {
                return false;
            }

            if( !(map.readAt(position.x, position.y).equals(targetColor)) ) {
                return false;
            }

            return true;
        }

        @Override
        public boolean inRange(Vector2d<Integer> position) {
            return map.inBounds(position);
        }

        private final Type replacementColor;
        private final Type targetColor;
        private final IMap2d<Type> map;
        private final IPixelSetListener pixelSetListener;
    }
    
    public static <Type> void fill(IMap2d<Type> map, Vector2d<Integer> centerPosition, final Type targetColor, final Type replacementColor, final boolean cross, IPixelSetListener pixelSetListener) {
        GeneralizedFloodFill.IFillExecutor fillExecutor = new SpecificExecutor<>(pixelSetListener, map, targetColor, replacementColor);
        GeneralizedFloodFill.fill(centerPosition, cross, fillExecutor);
    }
}
