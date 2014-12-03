package math;

public class Math
{
    public static float weightFloats(float valueA, float weightA, float valueB, float weightB)
    {
        return (valueA*weightA + valueB*weightB)/(weightA + weightB);
    }
}
