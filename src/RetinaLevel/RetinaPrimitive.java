package RetinaLevel;

import Datastructures.Vector2d;
import java.util.ArrayList;
import java.util.List;
import misc.Assert;

/**
 *
 * 
 */
public class RetinaPrimitive
{
    public Vector2d<Float> getNormalizedTangentForIntersectionTypeAndT(Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType, float f)
    {
        if( type == EnumType.LINESEGMENT )
        {
            return line.getNormalizedDirection();
        }
        else if( type == EnumType.CURVE )
        {
            // TODO< middle and pass over T and/or a type >
            
            if( intersectionPartnerType == Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN )
            {
                return curve.getNormalizedTangentAtEndpoint(0);
            }
            else if( intersectionPartnerType == Intersection.IntersectionPartner.EnumIntersectionEndpointType.END )
            {
                return curve.getNormalizedTangentAtEndpoint(1);
            }
            else
            {
                // TODO
                return curve.getNormalizedTangentAtEndpoint(0);
            }
        }
        
        throw new InternalError();
    }
    public enum EnumType
    {
        LINESEGMENT,
        CURVE
    }
    
    public EnumType type;
        
    public SingleLineDetector line;
    public ProcessG.Curve curve;
    public boolean marked = false; // used in algorithms for marking of the RetinaPrimitive for various algorithms
    
    private RetinaPrimitive()
    {
        
    }
    
    public static RetinaPrimitive makeLine(SingleLineDetector line)
    {
        RetinaPrimitive resultPrimitive;

        resultPrimitive = new RetinaPrimitive();
        resultPrimitive.line = line;
        resultPrimitive.type = EnumType.LINESEGMENT;

        return resultPrimitive;
    }

    public static RetinaPrimitive makeCurve(ProcessG.Curve curve)
    {
        RetinaPrimitive resultPrimitive;

        resultPrimitive = new RetinaPrimitive();
        resultPrimitive.curve = curve;
        resultPrimitive.type = EnumType.CURVE;

        return resultPrimitive;
    }
    
    public List<Intersection> getIntersections()
    {
        if( type == EnumType.LINESEGMENT )
        {
            return line.intersections;
        }

        throw new InternalError();
    }
    
    public Vector2d<Float> getNormalizedTangentOnEndpoint(int index)
    {
        Assert.Assert(index >= 0 && index <= 1, "");
        
        if( type == EnumType.LINESEGMENT )
        {
            return line.getNormalizedDirection();
        }
        else if( type == EnumType.CURVE )
        {
            return curve.getNormalizedTangentAtEndpoint(index);
        }
        
        throw new InternalError();
    }
}
