package RetinaLevel;

import Datastructures.Vector2d;

public class Intersection
{
    public enum EnumType
    {
        LINE,
        CURVE
    }
    
    public static class IntersectionPartner
    {
        public EnumType type;
        
        public SingleLineDetector line;
        public ProcessG.Curve curve;
        
        public static IntersectionPartner makeLine(SingleLineDetector line)
        {
            IntersectionPartner resultIntersection;
            
            resultIntersection = new IntersectionPartner();
            resultIntersection.line = line;
            resultIntersection.type = EnumType.LINE;
            
            return resultIntersection;
        }
        
        public static IntersectionPartner makeCurve(ProcessG.Curve curve)
        {
            IntersectionPartner resultIntersection;
            
            resultIntersection = new IntersectionPartner();
            resultIntersection.curve = curve;
            resultIntersection.type = EnumType.CURVE;
            
            return resultIntersection;
        }
    }
    
    public IntersectionPartner[] partners = new IntersectionPartner[2];
    
    public Vector2d<Integer> intersectionPosition;
}
