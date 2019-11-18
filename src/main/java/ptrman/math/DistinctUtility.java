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
import java.util.Collection;
import java.util.List;
import java.util.Random;

public enum DistinctUtility
{
	;

	public static List<Integer> getTwoDisjunctNumbers(Random random, int max)
    {
        int randomNumber;
        List<Integer> temporaryList;
        List<Integer> additionalNumbers;
        
        randomNumber = random.nextInt(max);
        temporaryList = new ArrayList<>();
        temporaryList.add(randomNumber);
        
        additionalNumbers = getDisjuctNumbersTo(random, temporaryList, 1, max);
        temporaryList.addAll(additionalNumbers);
        return temporaryList;
    }
    
    public static List<Integer> getDisjuctNumbersTo(Random random, Collection<Integer> numbers, int count, int max)
    {
        int counter;
        List<Integer> disjunctNumbers;
        
        disjunctNumbers = new ArrayList<>();
        
        for( counter = 0; counter < count; counter++ )
        {
            for(;;)
            {
                int randomNumber;
            
                randomNumber = random.nextInt(max);
                if( !numbers.contains(randomNumber) && !disjunctNumbers.contains(randomNumber) )
                {
                    disjunctNumbers.add(randomNumber);
                    break;
                }
            }
            
        }
        
        return disjunctNumbers;
    }
}
