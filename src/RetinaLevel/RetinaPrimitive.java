package RetinaLevel;

import Datastructures.Vector2d;
import java.util.ArrayList;
import misc.Assert;

/**
 *
 * 
 */
public class RetinaPrimitive
{

    public Vector2d<Float> getNormalizedTangentForIntersectionTypeAndT(Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType, float f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public enum EnumType
    {
        LINESEGMENT,
        CURVE
    }
    
    public EnumType type;
        
    public SingleLineDetector line;
    public ProcessG.Curve curve;
    
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
    
    public ArrayList<Intersection> getIntersections()
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
