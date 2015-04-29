package ptrman.misc;

import ptrman.Datastructures.Vector2d;
import static ptrman.Datastructures.Vector2d.FloatHelper.dot;

/**
 *
 *
 */
public class AngleHelper
{
    public static float getMinimalAngleInDegreeBetweenNormalizedVectors(Vector2d<Float> a, Vector2d<Float> b)
    {
        float dotResult, angleInRad, angleInDegree;
        
        dotResult = dot(a, b);
        angleInRad = (float)Math.acos(dotResult);
        angleInDegree = angleInRad * (360.0f/(2.0f*(float)Math.PI));
        
        if( angleInDegree > 90.0f )
        {
            angleInDegree = 180.0f - angleInDegree;
        }
        
        return angleInDegree;
    }
}
