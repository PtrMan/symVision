/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Datastructures;

import java.util.Arrays;

import static java.lang.System.arraycopy;

/**
 *
 * 
 */
public class Map2d<Type> implements IMap2d<Type>
{
    public Map2d(final int width, final int length)
    {
        this.width = width;
        this.length = length;
        this.array = (Type[])new Object[width*length];
    }

    @Override
    public void clear() {
        Arrays.fill(array, null);
    }

    public Type readAt(final int x, final int y)
    {
        assert inBounds(x, y) : "access error";
        
        return array[x + y*width];
    }
    
    public void setAt(final int x, final int y, final Type value)
    {
        assert inBounds(new Vector2d<>(x, y)) : "access error";
        
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
    public boolean inBounds(final Vector2d<Integer> position) {
        return position.x >= 0 && position.x < width && position.y >= 0 && position.y < length;
    }

    public boolean inBounds(final int px, final int py) {
        return px >= 0 && px < width && py >= 0 && py < length;
    }

    public Map2d<Type> copy()
    {

        final var cloned = new Map2d<Type>(width, length);
        arraycopy(array, 0, cloned.array, 0, array.length);
        
        return cloned;
    }
    
    private final int width;
    private final int length;
    private final Type[] array;
}
