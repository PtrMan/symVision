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
 * Abstraction over 2d-array used as image
 * 
 */
public class Map2d<Type> implements IMap2d<Type> {
    private final int width;
    private final int length;
    private final Type[] array;

    public Map2d(int width, int length) {
        this.width = width;
        this.length = length;
        this.array = (Type[])new Object[width*length];
    }

    @Override
    public void clear() {
        Arrays.fill(array, null);
    }

    public Type readAt(int x, int y) {
        if( !inBounds(x, y) ) {
            throw new RuntimeException("access error");
        }
        
        return array[x + y*width];
    }
    
    public void setAt(int x, int y, Type value) {
        if( !inBounds(new Vector2d<>(x, y)) ) {
            throw new RuntimeException("access error");
        }
        
        array[x + y*width] = value;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getLength() {
        return length;
    }

    @Override
    public boolean inBounds(Vector2d<Integer> position) {
        return position.x >= 0 && position.x < width && position.y >= 0 && position.y < length;
    }

    public boolean inBounds(int px, int py) {
        return px >= 0 && px < width && py >= 0 && py < length;
    }

    public Map2d<Type> copy() {

        Map2d<Type> cloned = new Map2d<>(width, length);
        arraycopy(array, 0, cloned.array, 0, array.length);
        
        return cloned;
    }
}
