/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.math;

import java.util.ArrayList;
import java.util.Random;

/**
 * 
 * Truncated fisher yades algorithm
 * http://stackoverflow.com/questions/2394246/algorithm-to-select-a-single-random-combination-of-values
 * 
 * Used for generating random elements, where the same element doesn't appear twice
 */
public class TruncatedFisherYades<Type>
{
    public interface IGenerator<Type>
    {
        Type generate(int index);
    }
    
    public TruncatedFisherYades(final int numberOfElements, final IGenerator<Type> generator)
    {

        for(int i = 0; i < numberOfElements; i++ )
            candidates.add(generator.generate(i));
    }
    
    public Type takeOne(final Random random)
    {

        final var chosenIndex = random.nextInt(candidates.size());
        final var result = candidates.get(chosenIndex);
        swapWithLast(chosenIndex);
        
        // remove last element
        candidates.remove(candidates.size()-1);
        
        return result;
    }
    
    private void swapWithLast(final int index)
    {
        Type element;

        final var lastIndex = candidates.size() - 1;
        swap(index, lastIndex);
    }
    
    private void swap(final int indexA, final int indexB)
    {

        final var element = candidates.get(indexA);
        candidates.set(indexA, candidates.get(indexB));
        candidates.set(indexB, element);
    }
    
    private final ArrayList<Type> candidates = new ArrayList<>();
}
