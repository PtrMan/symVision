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
import ptrman.math.ArrayRealVectorHelper;
import ptrman.math.Maths;
import ptrman.misc.AngleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * line detector
 *
 * Invariants:
 *    * a.x < b.x
 * 
 */
public class SingleLineDetector {
    final static AtomicInteger nextSerial = new AtomicInteger(0);

    static final double EPSILON = 0.001;
    private ArrayRealVector normalizedDirection;
    public final int serial;

    public double conf;

    private SingleLineDetector() {
        this.serial = nextSerial.addAndGet(1);
    }

    public static SingleLineDetector createFromFloatPositions(final ArrayRealVector a, final ArrayRealVector b, final double conf) {

        final var createdDetector = new SingleLineDetector();
        createdDetector.a = a;
        createdDetector.b = b;
        createdDetector.conf = conf;
        createdDetector.fullfillABInvariant();

        createdDetector.update();

        return createdDetector;
    }

    protected void update() {
        //TODO make constant only update when a and b changes
        normalizedDirection = a.subtract(b);

        //normalizedDirection.unitize();
        final var norm = normalizedDirection.getNorm();
        //throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
        if (norm == 0) normalizedDirection = new ArrayRealVector(2);
        else normalizedDirection.mapDivideToSelf(norm);
    }
    /**
     * 
     * swaps a and b if necessary to fulfill the invariant
     * 
     */
    private void fullfillABInvariant() {
        if( a.getEntry(0) > b.getEntry(0) ) {

            final var temp = a;
            a = b;
            b = temp;
        }
    }



    // orginal points, used to determine if a new point can be on the line or not
    public ArrayRealVector a;
    public ArrayRealVector b;

    public boolean resultOfCombination = false; // for visual debugging, was the detector combined from other detectors?

    public boolean marked = false; // used for marking in other (multithreading syncronous) processes
    public boolean markedPartOfCurve = false; // used for processG
    
    public final List<Intersection> intersections = new ArrayList<>();
    
    public boolean isBetweenOrginalStartAndEnd(final ArrayRealVector position) {
        final boolean value = !isYAxisSingularity();
        assert value : "ASSERT: " + "is singular!";

        // ASK< maybe the length claculation is unnecessary >

        var diffAB = b.subtract(a);
        final var diffAPosition = position.subtract(a);

        final var length = diffAB.getNorm();
        diffAB = ptrman.math.ArrayRealVectorHelper.normalize(diffAB);

        final var dotProduct = diffAB.dotProduct(diffAPosition);

        return dotProduct > 0.0f && dotProduct < length;
    }
    
    // TODO< rename to getA >
    public ArrayRealVector getAProjected() {
        return a;
    }
    
    // TODO< rename to getB >
    public ArrayRealVector getBProjected() {
        return b;
    }

    public ArrayRealVector getNormalizedDirection() {
        return normalizedDirection;
    }
    
    public ArrayRealVector projectPointOntoLine(final ArrayRealVector point, final ArrayRealVector result) {
        return isYAxisSingularity() ? projectPointOntoLineForSignular(point, result) : projectPointOntoLineForNonsignular(point, result);
    }
    
    protected final ArrayRealVector projectPointOntoLineForSignular(final ArrayRealVector point, final ArrayRealVector result) {
        final var r = result.getDataRef();
        r[0] = a.getEntry(0);
        r[1] = point.getEntry(1);
        return result;
    }

    private ArrayRealVector projectPointOntoLineForNonsignular(final ArrayRealVector point, final ArrayRealVector result) {
        final var lineDirection = getNormalizedDirection();
        final var diffFromAToPoint = point.subtract(new ArrayRealVector(new double[]{0.0f, getN()}));
        final var dotResult = lineDirection.dotProduct(diffFromAToPoint);


        ArrayRealVectorHelper.getScaled(lineDirection, dotResult, result);
        result.addToEntry(1, getN());
        return result;
    }
    
    public boolean isXOfPointInLine(final ArrayRealVector point) {
        final boolean value = !isYAxisSingularity();
        assert value : "ASSERT: " + "is singular!";

        final var pointData = point.getDataRef();
        return pointData[0] >= getAProjected().getEntry(0) && pointData[0] <= getBProjected().getEntry(0);
    }

    public double getN() {
        final boolean value = !isYAxisSingularity();
        assert value : "ASSERT: " + "is singular!";

        // TODO< m = inf special handling >
        final var aData = a.getDataRef();
        return aData[1] - aData[0]*getM();
    }

    public double getM() {
        final boolean value = !isYAxisSingularity();
        assert value : "ASSERT: " + "is singular!";

        final var aData = a.getDataRef();
        final var bData = b.getDataRef();
        return (bData[1]-aData[1]) / (bData[0]-aData[0]);
    }

    public double getLength() {
        return a.getDistance(b);
    }
    
    public static double getAngleBetween(final SingleLineDetector a, final SingleLineDetector b) {
        final var aNormalizedDirection = a.getNormalizedDirection();
        final var bNormalizedDirection = b.getNormalizedDirection();
        return AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(aNormalizedDirection, bNormalizedDirection);
    }

    double getTangentM() {
        final boolean value = !isYAxisSingularity();
        assert value : "ASSERT: " + "is singular!";

        final var m = getM();
        
        return -1.0f/m;
    }
    
    public static ArrayRealVector intersectLineDetectors(final SingleLineDetector lineA, final SingleLineDetector lineB) {
        if( !lineA.isYAxisSingularity() && !lineB.isYAxisSingularity() )
            return SingleLineDetector.intersectLinesMN(lineA.getM(), lineA.getN(), lineB.getM(), lineB.getN());
        else if( lineA.isYAxisSingularity() && !lineB.isYAxisSingularity() )
            intersectSingularLineWithMN(lineA, lineB.getM(), lineB.getN());
        else if( !lineA.isYAxisSingularity() && lineB.isYAxisSingularity() )
            intersectSingularLineWithMN(lineB, lineA.getM(), lineA.getN());
        
        // both lines are a singularity, there is no intersection
        return null;
    }
    
    // returns null if they are parallel
    public static ArrayRealVector intersectLinesMN(final double am, final double an, final double bm, final double bn) {
        // parallel check
        if( Maths.equals(am, bm, EPSILON) ) return null;
        
        final var x = (an - bn)/(bm - am);
        final var y = an + am * x;
        
        return new ArrayRealVector(new double[]{x, y}, false);
    }
    
    public static ArrayRealVector intersectLineWithMN(final SingleLineDetector line, final double am, final double an) {
        return line.isYAxisSingularity() ? intersectSingularLineWithMN(line, am, an) : intersectLinesMN(line.getM(), line.getN(), am, an);
    }
    
    private static ArrayRealVector intersectSingularLineWithMN(final SingleLineDetector line, final double am, final double an) {
        assert line.isYAxisSingularity() : "ASSERT: " + "must be signularity";

        final var y = am*line.getAProjected().getEntry(0) + an;
        
        return new ArrayRealVector(new double[]{line.getAProjected().getEntry(0), y}, false);
    }
    
    public boolean isYAxisSingularity() {
        //return Maths.equals(a.getEntry(0), b.getEntry(0), EPSILON);
        return a.getDataRef()[0] == b.getDataRef()[0];
    }
    
    

    public ArrayRealVector projectPointOntoLine(final ArrayRealVector point) {
        return projectPointOntoLine(point, new ArrayRealVector(2));
    }

    public ArrayRealVector getPositionOfEndpoint(final int index) {
        assert index == 0 || index == 1 : "ASSERT: " + "index must be 0 or 1";

        return index == 0 ? a : b;
    }
    
    // TODO< figure out if it is the middle >
    //  for this we need to restructure the function that it does
    //  * if the line is not a singularity
    //    -> compare x value if its before the begin or after the end, or in the middle
    //  * if the line is singularity we have to do the same with the y axis
    public Intersection.IntersectionPartner.EnumIntersectionEndpointType getIntersectionEndpoint(final ArrayRealVector point) {
        //ArrayRealVector diff;

        final var distanceBegin = a.getDistance(point);
        final var distanceEnd = b.getDistance(point);
//        diff = a.subtract(point);
//        distanceBegin = diff.getNorm();
//
//        diff = b.subtract(point);
//        distanceEnd = diff.getNorm();

        return distanceBegin < distanceEnd ? Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN : Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
    }
}
