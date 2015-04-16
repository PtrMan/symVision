package Datastructures;

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
    
    public Type readAt(int x, int y)
    {
        if( x < 0 || x >= width || y < 0 || y >= length )
        {
            throw new RuntimeException("access error");
        }
        
        return array[x + y*width];
    }
    
    public void setAt(int x, int y, Type value)
    {
        if( x < 0 || x > width || y < 0 || y > length )
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
