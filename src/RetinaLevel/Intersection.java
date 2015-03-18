package RetinaLevel;

import Datastructures.Vector2d;

public class Intersection
{
    public static class IntersectionPartner
    {
        public enum EnumIntersectionEndpointType
        {
            BEGIN,
            MIDDLE,
            END
        }
        
        public IntersectionPartner(RetinaPrimitive primitive, EnumIntersectionEndpointType intersectionEndpointType)
        {
            this.primitive = primitive;
            this.intersectionEndpointType = intersectionEndpointType;
        }
        
        public RetinaPrimitive primitive;
        public EnumIntersectionEndpointType intersectionEndpointType; 
    }
    
    public IntersectionPartner getOtherPartner(RetinaPrimitive primary)
    {
        if( primary.equals(partners[0]) )
        {
            return partners[1];
        }
        else
        {
            return partners[0];
        }
    }
    
    
    public IntersectionPartner[] partners = new IntersectionPartner[2];
    
    public Vector2d<Integer> intersectionPosition;
}
