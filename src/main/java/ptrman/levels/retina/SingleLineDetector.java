package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;
import ptrman.misc.AngleHelper;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Invariants:
 *    * a.x < b.x
 * 
 */
public class SingleLineDetector {
    private SingleLineDetector() {
    }

    public static SingleLineDetector createFromIntegerPositions(Vector2d<Integer> a, Vector2d<Integer> b) {
        SingleLineDetector createdDetector;

        createdDetector = new SingleLineDetector();
        createdDetector.a = new ArrayRealVector(new double[]{(double)a.x, (double)a.y});
        createdDetector.b = new ArrayRealVector(new double[]{(double)b.x, (double)b.y});
        createdDetector.fullfillABInvariant();
        //createdDetector.integratedSampleIndices = integratedSampleIndices;

        // calculate m, n
        // bug outruling
        //createdDetector.m = 0.0f; //(b.y-a.y)/(b.x-a.x);
        //createdDetector.n = 0.0f; //a.y - a.y * createdDetector.m;

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

        return createdDetector;
    }

    /**
     * 
     * swaps a and b if necessary to fullfil the invariant
     * 
     */
    private void fullfillABInvariant() {
        if( a.getDataRef()[0] > b.getDataRef()[0] ) {
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
        return new ArrayRealVector(a.subtract(b).unitVector());
    }
    
    public ArrayRealVector projectPointOntoLine(ArrayRealVector point) {
        if( isYAxisSingularity() ) {
            return projectPointOntoLineForSignular(point);
        }
        else {
            return projectPointOntoLineForNonsignular(point);
        }
    }
    
    private ArrayRealVector projectPointOntoLineForSignular(ArrayRealVector point) {
        return new ArrayRealVector(new double[]{ a.getDataRef()[0], point.getDataRef()[1]});
    }

    private ArrayRealVector projectPointOntoLineForNonsignular(ArrayRealVector point) {
        ArrayRealVector lineDirection = getNormalizedDirection();
        ArrayRealVector diffFromAToPoint = point.subtract(new ArrayRealVector(new double[]{0.0f, getN()}));
        double dotResult = lineDirection.dotProduct(diffFromAToPoint);

        return new ArrayRealVector(new double[]{0.0, getN()}).add(ptrman.math.ArrayRealVectorHelper.getScaled(lineDirection, dotResult));
    }
    
    public boolean isXOfPointInLine(ArrayRealVector point) {
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        return point.getDataRef()[0] >= getAProjected().getDataRef()[0] && point.getDataRef()[0] <= getBProjected().getDataRef()[0];
    }

    public double getN() {
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        // TODO< m = inf special handling >
        return a.getDataRef()[1] - a.getDataRef()[0]*getM();
    }

    public double getM() {
        Assert.Assert(!isYAxisSingularity(), "is singular!");

        ArrayRealVector diff = b.subtract(a);
        return diff.getDataRef()[1]/diff.getDataRef()[0];
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
        if( am == bm ) {
            return null;
        }
        
        double x = (an - bn)/(bm - am);
        double y = an + am * x;
        
        return new ArrayRealVector(new double[]{x, y});
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
        
        double y = am*line.getAProjected().getDataRef()[0] + an;
        
        return new ArrayRealVector(new double[]{line.getAProjected().getDataRef()[0], y});
    }
    
    public boolean isYAxisSingularity() {
        return a.getDataRef()[0] == b.getDataRef()[0];
    }
    
    
    
    // tests
    public static void unittestProjectPoint() {
        SingleLineDetector testLine;
        ArrayRealVector point;
        ArrayRealVector projectedPoint;
        
        testLine = SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{1.0f, 2.0f}), new ArrayRealVector(new double[]{2.0f, 3.0f}));
        point = new ArrayRealVector(new double[]{2.0f, 1.0f});
        
        projectedPoint = testLine.projectPointOntoLine(point);
        
        if( projectedPoint.getDataRef()[0] < 1.0f + 0.01f && projectedPoint.getDataRef()[0] > 1.0f - 0.01f && projectedPoint.getDataRef()[1] < 2.0f + 0.01f && projectedPoint.getDataRef()[1] > 2.0f - 0.01f ) {
            // all fine
        }
        else {
            throw new RuntimeException("Unittest failed (1)");
        }
        
        
        testLine = SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{1.0f, 2.0f}), new ArrayRealVector(new double[]{2.0f, 2.0f}));
        point = new ArrayRealVector(new double[]{2.0f, 1.0f});
        
        projectedPoint = testLine.projectPointOntoLine(point);
        
        if( projectedPoint.getDataRef()[0] < 2.0f + 0.01f && projectedPoint.getDataRef()[0] > 2.0f - 0.01f && projectedPoint.getDataRef()[1] < 2.0f + 0.01f && projectedPoint.getDataRef()[1] > 2.0f - 0.01f ) {
            // all fine
        }
        else {
            throw new RuntimeException("Unittest failed (1)");
        }
        
        testLine = SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{0.0f, 1.0f}), new ArrayRealVector(new double[]{1.0f, 0.0f}));
        point = new ArrayRealVector(new double[]{2.0f, 1.0f});
        
        projectedPoint = testLine.projectPointOntoLine(point);
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
        ArrayRealVector diff;
        double distanceBegin, distanceEnd;

        diff = a.subtract(point);
        distanceBegin = diff.getNorm();

        diff = b.subtract(point);
        distanceEnd = diff.getNorm();
        
        if( distanceBegin < distanceEnd ) {
            return Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN;
        }
        else {
            return Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
        }
    }
}
