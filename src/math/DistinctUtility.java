package math;

import java.util.ArrayList;
import java.util.Random;

public class DistinctUtility
{
    public static ArrayList<Integer> getTwoDisjunctNumbers(Random random, int max)
    {
        int randomNumber;
        ArrayList<Integer> temporaryList;
        
        randomNumber = random.nextInt(max);
        temporaryList = new ArrayList<>();
        temporaryList.add(randomNumber);
        
        getDisjuctNumbersTo(random, temporaryList, 1, max);
        return temporaryList;
    }
    
    public static void getDisjuctNumbersTo(Random random, ArrayList<Integer> numbers, int count, int max)
    {
        int counter;
        
        for( counter = 0; counter < count; counter++ )
        {
            for(;;)
            {
                int randomNumber;
            
                randomNumber = random.nextInt(max);
                if( !numbers.contains(randomNumber) )
                {
                    numbers.add(randomNumber);
                    break;
                }
            }
            
        }
    }
}
