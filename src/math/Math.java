package math;

public class Math
{
    public static float weightFloats(float valueA, float weightA, float valueB, float weightB)
    {
        return (valueA*weightA + valueB*weightB)/(weightA + weightB);
    }
    
    public static float power2(float x)
    {
        return x*x;
    }

    public static float squaredDistance(float x, float y)
    {
        return power2(x)+power2(y);
    }
    
    public static int faculty(int value)
    {
        int result, i;
        
        result = 1;
        
        for( i = 1; i < value; i++ )
        {
            result *= i;
        }
        
        return result;
    }
}
