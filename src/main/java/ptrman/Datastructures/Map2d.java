package ptrman.Datastructures;

import java.util.Arrays;

import static java.lang.System.arraycopy;

/**
 *
 * 
 */
public class Map2d<Type> implements IMap2d<Type>
{
    public Map2d(int width, int length)
    {
        this.width = width;
        this.length = length;
        this.array = (Type[])new Object[width*length];
    }

    @Override
    public void clear() {
        Arrays.fill(array, null);
    }

    public Type readAt(int x, int y)
    {
        if( !inBounds(x, y) )
        {
            throw new RuntimeException("access error");
        }
        
        return array[x + y*width];
    }
    
    public void setAt(int x, int y, Type value)
    {
        if( !inBounds(new Vector2d<>(x, y)) )
        {
            throw new RuntimeException("access error");
        }
        
        array[x + y*width] = value;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getLength()
    {
        return length;
    }

    @Override
    public boolean inBounds(Vector2d<Integer> position) {
        return position.x >= 0 && position.x < width && position.y >= 0 && position.y < length;
    }

    public boolean inBounds(int px, int py) {
        return px >= 0 && px < width && py >= 0 && py < length;
    }

    public Map2d<Type> copy()
    {
        Map2d<Type> cloned;
        
        cloned = new Map2d<>(width, length);
        arraycopy(array, 0, cloned.array, 0, array.length);
        
        return cloned;
    }
    
    private int width;
    private int length;
    private Type[] array;
}
