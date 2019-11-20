/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

        return primary.equals(p0.primitive) ? p1 : p0;
    }

}
