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

import java.util.List;

/**
 * primitive of the retina output
 * 
 */
public class RetinaPrimitive {
    public ArrayRealVector getNormalizedTangentForIntersectionTypeAndT(final Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType, final double f) {
        if( type == EnumType.LINESEGMENT ) return line.getNormalizedDirection();
        else // TODO< middle and pass over T and/or a type >
            if( type == EnumType.CURVE )
                if (intersectionPartnerType == Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN)
                    return curve.getNormalizedTangentAtEndpoint(0);
                else // TODO
                    if (intersectionPartnerType == Intersection.IntersectionPartner.EnumIntersectionEndpointType.END)
                        return curve.getNormalizedTangentAtEndpoint(1);
                    else return curve.getNormalizedTangentAtEndpoint(0);
        
        throw new InternalError();
    }

    public enum EnumType {
        LINESEGMENT,
        CURVE
    }
    
    public EnumType type;
        
    public SingleLineDetector line;
    public ProcessG.Curve curve;
    public boolean marked = false; // used in algorithms for marking of the RetinaPrimitive for various algorithms

    public int objectId = -1;

    public boolean hasValidObjectId() {
        return objectId != -1;
    }

    private RetinaPrimitive() {
    }

    public double retConf() {
        return line.conf;
    }
    
    public static RetinaPrimitive makeLine(final SingleLineDetector line) {

		final var resultPrimitive = new RetinaPrimitive();
        resultPrimitive.line = line;
        resultPrimitive.type = EnumType.LINESEGMENT;

        return resultPrimitive;
    }

    public static RetinaPrimitive makeCurve(final ProcessG.Curve curve) {

		final var resultPrimitive = new RetinaPrimitive();
        resultPrimitive.curve = curve;
        resultPrimitive.type = EnumType.CURVE;

        return resultPrimitive;
    }
    
    public List<Intersection> getIntersections() {
        if( type == EnumType.LINESEGMENT ) return line.intersections;

        throw new InternalError();
    }
    
    public ArrayRealVector getNormalizedTangentOnEndpoint(final int index) {
        assert index >= 0 && index <= 1 : "ASSERT: " + "";

        if( type == EnumType.LINESEGMENT ) return line.getNormalizedDirection();
        else if( type == EnumType.CURVE ) return curve.getNormalizedTangentAtEndpoint(index);
        
        throw new InternalError();
    }
}
