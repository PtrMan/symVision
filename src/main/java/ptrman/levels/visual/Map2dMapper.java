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

/**
 *
 */
public class Map2dMapper
{
    public interface IMap2dMapper<InputType, ResultType>
    {
        ResultType calculate(InputType value);
    }

    public static class Mapper<InputType, ResultType>
    {
        public void map(IMap2dMapper<InputType, ResultType> mapperImplementation, IMap2d<InputType> inputMap, IMap2d<ResultType> resultMap)
        {
            int x, y;

            for( y = 0; y < inputMap.getLength(); y++ )
            {
                for( x = 0; x < inputMap.getWidth(); x++ )
                {

					InputType tempInput = inputMap.readAt(x, y);
					ResultType tempResult = mapperImplementation.calculate(tempInput);
                    resultMap.setAt(x, y, tempResult);
                }
            }
        }
    }
}
