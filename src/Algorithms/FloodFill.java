package Algorithms;

import Datastructures.Map2d;
import Datastructures.Vector2d;

public class FloodFill<Type>
{
    public interface IPixelSetListener
    {
        void seted(Vector2d<Integer> position);
    }
    
    public void fill(Map2d<Type> map, Vector2d<Integer> position, Vector2d<Integer> mapSize, Type targetColor, Type replacementColor, IPixelSetListener pixelSetListener)
    {
        if( targetColor.equals(replacementColor) )
        {
            return;
        }
        
        if( !(map.readAt(position.x, position.y).equals(targetColor)) )
        {
            return;
        }
        
        map.setAt(position.x, position.y, replacementColor);
        
        pixelSetListener.seted(position);
        
        fillRangeChecked(map, new Vector2d<>(position.x+1, position.y), mapSize, targetColor, replacementColor, pixelSetListener);
        fillRangeChecked(map, new Vector2d<>(position.x-1, position.y), mapSize, targetColor, replacementColor, pixelSetListener);
        fillRangeChecked(map, new Vector2d<>(position.x, position.y+1), mapSize, targetColor, replacementColor, pixelSetListener);
        fillRangeChecked(map, new Vector2d<>(position.x, position.y-1), mapSize, targetColor, replacementColor, pixelSetListener);
    }

    private void fillRangeChecked(Map2d<Type> map, Vector2d<Integer> position, Vector2d<Integer> mapSize, Type targetColor, Type replacementColor, IPixelSetListener pixelSetListener)
    {
        if( position.x < 0 || position.x >= mapSize.x )
        {
            return;
        }
        
        if( position.y < 0 || position.y >= mapSize.y )
        {
            return;
        }
        
        fill(map, position, mapSize, targetColor, replacementColor, pixelSetListener);
    }
}
