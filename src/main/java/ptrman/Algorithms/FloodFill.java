package ptrman.Algorithms;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayDeque;
import java.util.Queue;

public class FloodFill
{
    public interface IPixelSetListener
    {
        void seted(Vector2d<Integer> position);
    }
    
    public static <Type> void fill(IMap2d<Type> map, Vector2d<Integer> centerPosition, final Type targetColor, final Type replacementColor, final boolean cross, IPixelSetListener pixelSetListener)
    {
        Queue<Vector2d<Integer>> remainingPositions = new ArrayDeque<>();
        remainingPositions.add(centerPosition);

        for(;;) {
            if( remainingPositions.isEmpty() ) {
                break;
            }

            final Vector2d<Integer> currentPosition = remainingPositions.poll();

            if( targetColor.equals(replacementColor) )
            {
                return;
            }

            if( !(map.readAt(currentPosition.x, currentPosition.y).equals(targetColor)) )
            {
                return;
            }

            map.setAt(currentPosition.x, currentPosition.y, replacementColor);

            pixelSetListener.seted(currentPosition);

            fillRangeChecked(map, new Vector2d<>(currentPosition.x+1, currentPosition.y), remainingPositions);
            fillRangeChecked(map, new Vector2d<>(currentPosition.x-1, currentPosition.y), remainingPositions);
            fillRangeChecked(map, new Vector2d<>(currentPosition.x, currentPosition.y+1), remainingPositions);
            fillRangeChecked(map, new Vector2d<>(currentPosition.x, currentPosition.y-1), remainingPositions);

            if( cross ) {
                fillRangeChecked(map, new Vector2d<>(currentPosition.x+1, currentPosition.y+1), remainingPositions);
                fillRangeChecked(map, new Vector2d<>(currentPosition.x-1, currentPosition.y+1), remainingPositions);
                fillRangeChecked(map, new Vector2d<>(currentPosition.x+1, currentPosition.y-1), remainingPositions);
                fillRangeChecked(map, new Vector2d<>(currentPosition.x-1, currentPosition.y-1), remainingPositions);
            }
        }
    }

    private static <Type> void fillRangeChecked(final IMap2d<Type> map, final Vector2d<Integer> position, Queue<Vector2d<Integer>> remainingPositions)
    {
        if( !map.inBounds(position) ) {
            return;
        }

        remainingPositions.add(position);
    }
}
