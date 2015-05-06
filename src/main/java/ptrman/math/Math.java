package ptrman.math;

public class Math {
    public static float weightFloats(final float valueA, final float weightA, final float valueB, final float weightB) {
        return (valueA*weightA + valueB*weightB)/(weightA + weightB);
    }

    public static double weightDoubles(final double valueA, final double weightA, final double valueB, final double weightB) {
        return (valueA*weightA + valueB*weightB)/(weightA + weightB);
    }
    
    public static float power2(float x)
    {
        return x*x;
    }

    // SUPERCOMPILATION candidate
    public static float squaredDistance(float[] data)
    {
        float result;
        int i;

        // we play supercompiler
        // SUPERCOMPILATION remove this when we use supercompilation
        if( data.length == 2 )
        {
            return power2(data[0]) + power2(data[1]);
        }

        result = 0.0f;

        for( i = 0; i < data.length; i++ )
        {
            result += power2(data[i]);
        }

        return result;
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

    public static float clamp01(float value)
    {
        return java.lang.Math.min(1.0f, java.lang.Math.max(value, 0.0f));
    }

    public static int clampInt(final int value, final int min, final int max) {
        return java.lang.Math.min(max, java.lang.Math.max(value, min));
    }

    public static int modNegativeWraparound(final int value, final int max) {
        if( value >= 0 ) {
            return value % max;
        }
        else {
            final int positiveValue = -value;
            final int positiveValueMod = positiveValue % max;
            return (max - positiveValueMod) % max;
        }
    }
}
