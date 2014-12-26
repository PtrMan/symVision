package math;

import java.util.ArrayList;
import java.util.Random;

public class DistinctUtility
{
    public static ArrayList<Integer> getTwoDisjunctNumbers(Random random, int max)
    {
        int randomNumber;
        ArrayList<Integer> temporaryList;
        ArrayList<Integer> additionalNumbers;
        
        randomNumber = random.nextInt(max);
        temporaryList = new ArrayList<>();
        temporaryList.add(randomNumber);
        
        additionalNumbers = getDisjuctNumbersTo(random, temporaryList, 1, max);
        temporaryList.addAll(additionalNumbers);
        return temporaryList;
    }
    
    public static ArrayList<Integer> getDisjuctNumbersTo(Random random, ArrayList<Integer> numbers, int count, int max)
    {
        int counter;
        ArrayList<Integer> disjunctNumbers;
        
        disjunctNumbers = new ArrayList<Integer>();
        
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
