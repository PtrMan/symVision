/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;

public class Map2dTransform<Type>
{
    public void magnify(IMap2d<Type> input, IMap2d<Type> output, int factor)
    {
        int ix, iy;

        for( iy = 0; iy < input.getLength(); iy++ )
        {
            for( ix = 0; ix < input.getWidth(); ix++ )
            {
                writeMagnified(output, ix, iy, input.readAt(ix, iy), factor);
            }
        }
    }

    private void writeMagnified(IMap2d<Type> output, int ix, int iy, Type value, int factor)
    {
        int writeX, writeY;

        for( writeY = 0; writeY < factor; writeY++ )
        {
            for( writeX = 0; writeX < factor; writeX++ )
            {
                output.setAt(ix*factor + writeX, iy*factor + writeY, value);
            }
        }
    }
}
