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

import ptrman.Datastructures.Vector2d;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Flood fill algorithm which is a bit more generalized
 */
public enum GeneralizedFloodFill {
	;

	public interface IFillExecutor {
        void fillAt(final Vector2d<Integer> position, final Vector2d<Integer> fromDirection);

        boolean canAndShouldBeFilled(final Vector2d<Integer> position);

        boolean inRange(final Vector2d<Integer> position);
    }

    private static class QueueElement {
        public QueueElement(final Vector2d<Integer> position, final Vector2d<Integer> fromDirection) {
            this.position = position;
            this.fromDirection = fromDirection;
        }

        final public Vector2d<Integer> position;
        final public Vector2d<Integer> fromDirection;
    }

    public static void fill(final Vector2d<Integer> centerPosition, final boolean cross, final IFillExecutor fillExecutor) {
        final Deque<QueueElement> queue = new ArrayDeque<>();
        queue.push(new QueueElement(centerPosition, new Vector2d<>(0, 0)));

        while (!queue.isEmpty()) {

            final var currentQueueElement = queue.poll();
            final var currentPosition = currentQueueElement.position;

            if (!fillExecutor.canAndShouldBeFilled(currentPosition)) continue;

            fillExecutor.fillAt(currentPosition, currentQueueElement.fromDirection);

            fillRangeChecked(currentPosition, new Vector2d<>(-1, 0), queue, fillExecutor);
            fillRangeChecked(currentPosition, new Vector2d<>(+1, 0), queue, fillExecutor);
            fillRangeChecked(currentPosition, new Vector2d<>(0, -1), queue, fillExecutor);
            fillRangeChecked(currentPosition, new Vector2d<>(0, +1), queue, fillExecutor);

            if (cross) {
                fillRangeChecked(currentPosition, new Vector2d<>(-1, -1), queue, fillExecutor);
                fillRangeChecked(currentPosition, new Vector2d<>(+1, -1), queue, fillExecutor);
                fillRangeChecked(currentPosition, new Vector2d<>(-1, +1), queue, fillExecutor);
                fillRangeChecked(currentPosition, new Vector2d<>(+1, +1), queue, fillExecutor);
            }
        }
    }

    private static void fillRangeChecked(final Vector2d<Integer> currentPosition, final Vector2d<Integer> fromDirection, final Deque<QueueElement> queue, final IFillExecutor fillExecutor) {
        final var position = Vector2d.IntegerHelper.sub(currentPosition, fromDirection);

        if( !fillExecutor.inRange(position) ) return;

        queue.push(new QueueElement(position, fromDirection));
    }
}
