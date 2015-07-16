package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;

public class Intersection  {


    public final IntersectionPartner p0, p1;

    public final ArrayRealVector intersectionPosition;

    public Intersection(ArrayRealVector pos, IntersectionPartner part0, IntersectionPartner part1) {
        this.intersectionPosition = pos;
        this.p0 = part0;
        this.p1 = part1;
    }

    public static class IntersectionPartner {
        public enum EnumIntersectionEndpointType {
            BEGIN,
            MIDDLE,
            END
        }
        
        public IntersectionPartner(RetinaPrimitive primitive, EnumIntersectionEndpointType intersectionEndpointType) {
            this.primitive = primitive;
            this.intersectionEndpointType = intersectionEndpointType;
        }
        
        public final RetinaPrimitive primitive;
        public final EnumIntersectionEndpointType intersectionEndpointType;
    }


    public IntersectionPartner getOtherPartner(RetinaPrimitive primary) {

        if( primary.equals(p0.primitive) ) {
            return p1;
        }
        else {
            return p0;
        }
    }

}
