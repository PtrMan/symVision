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
import ptrman.Datastructures.Vector2d;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.math.Maths;
import ptrman.misc.AngleHelper;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
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

    private SingleLineDetector() {
        this.serial = nextSerial.addAndGet(1);
    }

    public static SingleLineDetector createFromIntegerPositions(Vector2d<Integer> a, Vector2d<Integer> b) {
        SingleLineDetector createdDetector;

        createdDetector = new SingleLineDetector();
        createdDetector.a = new ArrayRealVector(new double[]{(double)a.x, (double)a.y}, false);
        createdDetector.b = new ArrayRealVector(new double[]{(double)b.x, (double)b.y}, false);
        createdDetector.fullfillABInvariant();
        //createdDetector.integratedSampleIndices = integratedSampleIndices;

        // calculate m, n
        // bug outruling
        //createdDetector.m = 0.0f; //(b.y-a.y)/(b.x-a.x);
        //createdDetector.n = 0.0f; //a.y - a.y * createdDetector.m;

        createdDetector.update();

        return createdDetector;
    }

    public static SingleLineDetector createFromFloatPositions(ArrayRealVector a, ArrayRealVector b) {
        SingleLineDetector createdDetector;

        createdDetector = new SingleLineDetector();
        createdDetector.a = a;
        createdDetector.b = b;
        createdDetector.fullfillABInvariant();
        //createdDetector.integratedSampleIndices = integratedSampleIndices;

        // calculate m, n
        // bug outruling
        //createdDetector.m = 0.0f; //(b.y-a.y)/(b.x-a.x);
        //createdDetector.n = 0.0f; //a.y - a.y * createdDetector.m;

        createdDetector.update();

        return createdDetector;
    }

    protected void update() {
        //TODO make constant only update when a and b changes
        normalizedDirection = a.subtract(b);

        //normalizedDirection.unitize();
        final double norm = normalizedDirection.getNorm();
        if (norm == 0) {
            normalizedDirection = new ArrayRealVector(2);
            //throw new MathArithmeticException(LocalizedFormats.ZERO_NORM);
        }
        else {
            normalizedDirection.mapDivideToSelf(norm);
        }
    }
    /**
     * 
     * swaps a and b if necessary to fulfill the invariant
     * 
     */
    private void fullfillABInvariant() {
        if( a.getEntry(0) > b.getEntry(0) ) {
            ArrayRealVector temp;

            temp = a;
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
    
    public List<Intersection> intersections = new ArrayList<>();
    
    public boolean isBetweenOrginalStartAndEnd(ArrayRealVector position) {
        Assert.Assert(!isYAxisSingularity(), "is singular!");

        // ASK< maybe the length claculation is unnecessary >

        ArrayRealVector diffAB = b.subtract(a);
        ArrayRealVector diffAPosition = position.subtract(a);

        double length = diffAB.getNorm();
        diffAB = ptrman.math.ArrayRealVectorHelper.normalize(diffAB);

        double dotProduct = diffAB.dotProduct(diffAPosition);

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
    
    public ArrayRealVector projectPointOntoLine(ArrayRealVector point, ArrayRealVector result) {
        if( isYAxisSingularity() ) {
            return projectPointOntoLineForSignular(point, result);
        }
        else {
            return projectPointOntoLineForNonsignular(point, result);
        }
    }
    
    protected final ArrayRealVector projectPointOntoLineForSignular(ArrayRealVector point, ArrayRealVector result) {
        double[] r = result.getDataRef();
        r[0] = a.getEntry(0);
        r[1] = point.getEntry(1);
        return result;
    }

    private ArrayRealVector projectPointOntoLineForNonsignular(ArrayRealVector point, ArrayRealVector result) {
        ArrayRealVector lineDirection = getNormalizedDirection();
        ArrayRealVector diffFromAToPoint = point.subtract(new ArrayRealVector(new double[]{0.0f, getN()}));
        double dotResult = lineDirection.dotProduct(diffFromAToPoint);


        ArrayRealVectorHelper.getScaled(lineDirection, dotResult, result);
        result.addToEntry(1, getN());
        return result;
    }
    
    public boolean isXOfPointInLine(ArrayRealVector point) {
        Assert.Assert(!isYAxisSingularity(), "is singular!");

        double[] pointData = point.getDataRef();
        return pointData[0] >= getAProjected().getEntry(0) && pointData[0] <= getBProjected().getEntry(0);
    }

    public double getN() {
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        // TODO< m = inf special handling >
        double[] aData = a.getDataRef();
        return aData[1] - aData[0]*getM();
    }

    public double getM() {
        Assert.Assert(!isYAxisSingularity(), "is singular!");

        double[] aData = a.getDataRef();
        double[] bData = b.getDataRef();
        return (bData[1]-aData[1]) / (bData[0]-aData[0]);
    }

    public double getLength() {
        return a.getDistance(b);
    }
    
    public static double getAngleBetween(SingleLineDetector a, SingleLineDetector b) {
        ArrayRealVector aNormalizedDirection = a.getNormalizedDirection();
        ArrayRealVector bNormalizedDirection = b.getNormalizedDirection();
        return AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(aNormalizedDirection, bNormalizedDirection);
    }

    double getTangentM() {
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        double m = getM();
        
        return -1.0f/m;
    }
    
    public static ArrayRealVector intersectLineDetectors(SingleLineDetector lineA, SingleLineDetector lineB) {
        if( !lineA.isYAxisSingularity() && !lineB.isYAxisSingularity() ) {
            return SingleLineDetector.intersectLinesMN(lineA.getM(), lineA.getN(), lineB.getM(), lineB.getN());
        }
        else if( lineA.isYAxisSingularity() && !lineB.isYAxisSingularity() ) {
            intersectSingularLineWithMN(lineA, lineB.getM(), lineB.getN());
        }
        else if( !lineA.isYAxisSingularity() && lineB.isYAxisSingularity() ) {
            intersectSingularLineWithMN(lineB, lineA.getM(), lineA.getN());
        }
        
        // both lines are a singularity, there is no intersection
        return null;
    }
    
    // returns null if they are parallel
    public static ArrayRealVector intersectLinesMN(double am, double an, double bm, double bn) {
        // parallel check
        if( Maths.equals(am, bm, EPSILON) ) {
            return null;
        }
        
        double x = (an - bn)/(bm - am);
        double y = an + am * x;
        
        return new ArrayRealVector(new double[]{x, y}, false);
    }
    
    public static ArrayRealVector intersectLineWithMN(SingleLineDetector line, double am, double an) {
        if( line.isYAxisSingularity() ) {
            return intersectSingularLineWithMN(line, am, an);
        }
        else {
            return intersectLinesMN(line.getM(), line.getN(), am, an);
        }
    }
    
    private static ArrayRealVector intersectSingularLineWithMN(SingleLineDetector line, double am, double an) {
        Assert.Assert(line.isYAxisSingularity(), "must be signularity");
        
        double y = am*line.getAProjected().getEntry(0) + an;
        
        return new ArrayRealVector(new double[]{line.getAProjected().getEntry(0), y}, false);
    }
    
    public boolean isYAxisSingularity() {
        //return Maths.equals(a.getEntry(0), b.getEntry(0), EPSILON);
        return a.getDataRef()[0] == b.getDataRef()[0];
    }
    
    

    public ArrayRealVector projectPointOntoLine(ArrayRealVector point) {
        return projectPointOntoLine(point, new ArrayRealVector(2));
    }

    public ArrayRealVector getPositionOfEndpoint(int index) {
        Assert.Assert(index == 0 || index == 1, "index must be 0 or 1");
        
        if( index == 0 ) {
            return a;
        }
        else {
            return b;
        }
    }
    
    // TODO< figure out if it is the middle >
    //  for this we need to restructure the function that it does
    //  * if the line is not a singularity
    //    -> compare x value if its before the begin or after the end, or in the middle
    //  * if the line is singularity we have to do the same with the y axis
    public Intersection.IntersectionPartner.EnumIntersectionEndpointType getIntersectionEndpoint(ArrayRealVector point) {
        //ArrayRealVector diff;
        final double distanceBegin, distanceEnd;

        distanceBegin = a.getDistance(point);
        distanceEnd = b.getDistance(point);
//        diff = a.subtract(point);
//        distanceBegin = diff.getNorm();
//
//        diff = b.subtract(point);
//        distanceEnd = diff.getNorm();
        
        if( distanceBegin < distanceEnd ) {
            return Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN;
        }
        else {
            return Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
        }
    }
}
