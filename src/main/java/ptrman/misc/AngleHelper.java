package ptrman.misc;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 *
 */
public class AngleHelper
{
    public static double getMinimalAngleInDegreeBetweenNormalizedVectors(ArrayRealVector a, ArrayRealVector b)
    {
        double dotResult = a.dotProduct(b);
        double angleInRad = Math.acos(dotResult);
        double angleInDegree = angleInRad * (360.0f/(2.0f*(float)Math.PI));
        
        if( angleInDegree > 90.0f )
        {
            angleInDegree = 180.0f - angleInDegree;
        }
        
        return angleInDegree;
    }
}
