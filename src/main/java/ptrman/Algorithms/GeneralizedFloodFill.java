package ptrman.Algorithms;

import ptrman.Datastructures.Vector2d;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Flood fill algorithm which is a bit more generalized
 */
public class GeneralizedFloodFill {
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

    public static void fill(Vector2d<Integer> centerPosition, final boolean cross, IFillExecutor fillExecutor) {
        Deque<QueueElement> queue = new ArrayDeque<>();
        queue.push(new QueueElement(centerPosition, new Vector2d<>(0, 0)));

        for(;;) {
            if( queue.isEmpty() ) {
                break;
            }

            final QueueElement currentQueueElement = queue.poll();
            final Vector2d<Integer> currentPosition = currentQueueElement.position;

            if( !fillExecutor.canAndShouldBeFilled(currentPosition) ) {
                continue;
            }

            fillExecutor.fillAt(currentPosition, currentQueueElement.fromDirection);

            fillRangeChecked(currentPosition, new Vector2d<>(-1, 0), queue, fillExecutor);
            fillRangeChecked(currentPosition, new Vector2d<>(+1, 0), queue, fillExecutor);
            fillRangeChecked(currentPosition, new Vector2d<>(0, -1), queue, fillExecutor);
            fillRangeChecked(currentPosition, new Vector2d<>(0, +1), queue, fillExecutor);

            if( cross ) {
                fillRangeChecked(currentPosition, new Vector2d<>(-1, -1), queue, fillExecutor);
                fillRangeChecked(currentPosition, new Vector2d<>(+1, -1), queue, fillExecutor);
                fillRangeChecked(currentPosition, new Vector2d<>(-1, +1), queue, fillExecutor);
                fillRangeChecked(currentPosition, new Vector2d<>(+1, +1), queue, fillExecutor);
            }
        }
    }

    private static void fillRangeChecked(final Vector2d<Integer> currentPosition, final Vector2d<Integer> fromDirection, Deque<QueueElement> queue, IFillExecutor fillExecutor) {
        final Vector2d<Integer> position = Vector2d.IntegerHelper.sub(currentPosition, fromDirection);

        if( !fillExecutor.inRange(position) ) {
            return;
        }

        queue.push(new QueueElement(position, fromDirection));
    }
}
