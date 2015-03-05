package math;

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
    
    public TruncatedFisherYades(int numberOfElements, IGenerator<Type> generator)
    {
        int i;
        
        for( i = 0; i < numberOfElements; i++ )
        {
            candidates.add(generator.generate(i));
        }
    }
    
    public Type takeOne(Random random)
    {
        int chosenIndex;
        Type result;
        
        chosenIndex = random.nextInt(candidates.size());
        result = candidates.get(chosenIndex);
        swapWithLast(chosenIndex);
        
        // remove last element
        candidates.remove(candidates.size()-1);
        
        return result;
    }
    
    private void swapWithLast(int index)
    {
        Type element;
        int lastIndex;
        
        lastIndex = candidates.size()-1;
        swap(index, lastIndex);
    }
    
    private void swap(int indexA, int indexB)
    {
        Type element;
        
        element = candidates.get(indexA);
        candidates.set(indexA, candidates.get(indexB));
        candidates.set(indexB, element);
    }
    
    private ArrayList<Type> candidates = new ArrayList<>();
}
