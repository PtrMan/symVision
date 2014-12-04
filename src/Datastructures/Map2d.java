package Datastructures;

/**
 *
 * 
 */
public class Map2d<Type>
{
    public Map2d(int width, int length)
    {
        this.width = width;
        this.length = length;
        this.array = (Type[])new Object[width*length];
    }
    
    public Type getAt(int x, int y)
    {
        if( x < 0 || x > width || y < 0 || y > length )
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
    
    private int width;
    private int length;
    private Type[] array;
}
