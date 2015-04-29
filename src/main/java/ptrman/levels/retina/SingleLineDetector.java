package ptrman.levels.retina;

import ptrman.Datastructures.Vector2d;
import static ptrman.Datastructures.Vector2d.FloatHelper.add;
import static ptrman.Datastructures.Vector2d.FloatHelper.dot;
import static ptrman.Datastructures.Vector2d.FloatHelper.getScaled;
import static ptrman.Datastructures.Vector2d.FloatHelper.sub;
import java.util.ArrayList;
import ptrman.misc.AngleHelper;
import ptrman.misc.Assert;

/**
 * 
 * Invariants:
 *    * a.x < b.x
 * 
 */
public class SingleLineDetector
{
    private SingleLineDetector()
    {
    }

    public static SingleLineDetector createFromIntegerPositions(Vector2d<Integer> a, Vector2d<Integer> b)
    {
        SingleLineDetector createdDetector;

        createdDetector = new SingleLineDetector();
        createdDetector.aFloat = new Vector2d<Float>((float)a.x, (float)a.y);
        createdDetector.bFloat = new Vector2d<Float>((float)b.x, (float)b.y);
        createdDetector.fullfillABInvariant();
        //createdDetector.integratedSampleIndices = integratedSampleIndices;

        // calculate m, n
        // bug outruling
        //createdDetector.m = 0.0f; //(b.y-a.y)/(b.x-a.x);
        //createdDetector.n = 0.0f; //a.y - a.y * createdDetector.m;

        return createdDetector;
    }

    public static SingleLineDetector createFromFloatPositions(Vector2d<Float> a, Vector2d<Float> b)
    {
        SingleLineDetector createdDetector;

        createdDetector = new SingleLineDetector();
        createdDetector.aFloat = a;
        createdDetector.bFloat = b;
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
    private void fullfillABInvariant()
    {
        if( aFloat.x > bFloat.x )
        {
            Vector2d<Float> temp;

            temp = aFloat;
            aFloat = bFloat;
            bFloat = temp;
        }
    }



    // orginal points, used to determine if a new point can be on the line or not
    public Vector2d<Float> aFloat;
    public Vector2d<Float> bFloat;

    public boolean resultOfCombination = false; // for visual debugging, was the detector combined from other detectors?

    public boolean marked = false; // used for marking in other (multithreading syncronous) processes
    public boolean markedPartOfCurve = false; // used for processG
    
    public ArrayList<Intersection> intersections = new ArrayList<>();
    
    public boolean isBetweenOrginalStartAndEnd(Vector2d<Float> position)
    {
        Vector2d<Float> diffAB, diffABnormalizd, diffAPosition;
        float length;
        float dotProduct;
        
        Assert.Assert(!isYAxisSingularity(), "is singular!");

        // ASK< maybe the length claculation is unnecessary >

        diffAB = new Vector2d<Float>(bFloat.x - aFloat.x, bFloat.y - aFloat.y);
        diffAPosition = new Vector2d<Float>(position.x - aFloat.x, position.y - aFloat.y);

        length = (float)Math.sqrt(diffAB.x*diffAB.x + diffAB.y*diffAB.y);
        diffAB.x /= length;
        diffAB.y /= length;

        dotProduct = diffAB.x * diffAPosition.x + diffAB.y * diffAPosition.y;

        return dotProduct > 0.0f && dotProduct < length;
    }
    
    // TODO< rename to getA >
    public Vector2d<Float> getAProjected()
    {
        return aFloat;
    }
    
    // TODO< rename to getB >
    public Vector2d<Float> getBProjected()
    {
        return bFloat;
    }

    public Vector2d<Float> getNormalizedDirection()
    {
        Vector2d<Float> diff;

        diff = Vector2d.FloatHelper.sub(getAProjected(), getBProjected());
        return Vector2d.FloatHelper.normalize(diff);
    }
    
    public Vector2d<Float> projectPointOntoLine(Vector2d<Float> point)
    {
        if( isYAxisSingularity() )
        {
            return projectPointOntoLineForSignular(point);
        }
        else
        {
            return projectPointOntoLineForNonsignular(point);
        }
    }
    
    private Vector2d<Float> projectPointOntoLineForSignular(Vector2d<Float> point)
    {
        return new Vector2d<Float>(aFloat.x, point.y);
    }

    private Vector2d<Float> projectPointOntoLineForNonsignular(Vector2d<Float> point)
    {
        Vector2d<Float> lineDirection;
        Vector2d<Float> diffFromAToPoint;
        float dotResult;

        lineDirection = getNormalizedDirection();
        diffFromAToPoint = sub(point, new Vector2d<>(0.0f, getN()));
        dotResult = dot(lineDirection, diffFromAToPoint);

        return add(new Vector2d<>(0.0f, getN()), getScaled(lineDirection, dotResult));
    }
    
    public boolean isXOfPointInLine(Vector2d<Float> point)
    {
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        return point.x >= getAProjected().x && point.x <= getBProjected().x;
    }

    public float getN()
    {
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        // TODO< m = inf special handling >
        return aFloat.y - aFloat.x*getM();
    }

    public float getM()
    {
        Vector2d<Float> diff;
        
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        diff = sub(bFloat, aFloat);
        return diff.y/diff.x;
    }

    public float getLength() {
        Vector2d<Float> diff;
        
        diff = sub(aFloat, bFloat);
        
        return ptrman.Datastructures.Vector2d.FloatHelper.getLength(diff);
    }
    
    public static float getAngleBetween(SingleLineDetector a, SingleLineDetector b)
    {
        Vector2d<Float> aNormalizedDirection, bNormalizedDirection;
        
        aNormalizedDirection = a.getNormalizedDirection();
        bNormalizedDirection = b.getNormalizedDirection();
        return AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(aNormalizedDirection, bNormalizedDirection);
    }

    float getTangentM()
    {
        float m;
        
        Assert.Assert(!isYAxisSingularity(), "is singular!");
        
        m = getM();
        
        return -1.0f/m;
    }
    
    public static Vector2d<Float> intersectLineDetectors(SingleLineDetector lineA, SingleLineDetector lineB)
    {
        Assert.Assert(!lineA.isYAxisSingularity() || !lineB.isYAxisSingularity(), "both lines do have a singularity!");
        
        if( !lineA.isYAxisSingularity() && !lineB.isYAxisSingularity() )
        {
            return SingleLineDetector.intersectLinesMN(lineA.getM(), lineA.getN(), lineB.getM(), lineB.getN());
        }
        else if( lineA.isYAxisSingularity() && !lineB.isYAxisSingularity() )
        {
            intersectSingularLineWithMN(lineA, lineB.getM(), lineB.getN());
        }
        else if( !lineA.isYAxisSingularity() && lineB.isYAxisSingularity() )
        {
            intersectSingularLineWithMN(lineB, lineA.getM(), lineA.getN());
        }
        
        throw new RuntimeException("Internal Error");
    }
    
    // returns null if they are parallel
    public static Vector2d<Float> intersectLinesMN(float am, float an, float bm, float bn)
    {
        float x, y;
        
        // parallel check
        if( am == bm )
        {
            return null;
        }
        
        x = (an - bn)/(bm - am);
        y = an + am * x;
        
        return new Vector2d<Float>(x, y);
    }
    
    public static Vector2d<Float> intersectLineWithMN(SingleLineDetector line, float am, float an)
    {
        if( line.isYAxisSingularity() )
        {
            return intersectSingularLineWithMN(line, am, an);
        }
        else
        {
            return intersectLinesMN(line.getM(), line.getN(), am, an);
        }
    }
    
    private static Vector2d<Float> intersectSingularLineWithMN(SingleLineDetector line, float am, float an)
    {
        float y;
        
        Assert.Assert(line.isYAxisSingularity(), "must be signularity");
        
        y = am*line.getAProjected().x + an;
        
        return new Vector2d<Float>(line.getAProjected().x, y);
    }
    
    public boolean isYAxisSingularity()
    {
        return aFloat.x == bFloat.x;
    }
    
    
    
    // tests
    public static void unittestProjectPoint()
    {
        SingleLineDetector testLine;
        Vector2d<Float> point;
        Vector2d<Float> projectedPoint;
        
        testLine = SingleLineDetector.createFromFloatPositions(new Vector2d<Float>(1.0f, 2.0f), new Vector2d<Float>(2.0f, 3.0f));
        point = new Vector2d<Float>(2.0f, 1.0f);
        
        projectedPoint = testLine.projectPointOntoLine(point);
        
        if( projectedPoint.x < 1.0f + 0.01f && projectedPoint.x > 1.0f - 0.01f && projectedPoint.y < 2.0f + 0.01f && projectedPoint.y > 2.0f - 0.01f ) 
        {
            // all fine
        }
        else
        {
            throw new RuntimeException("Unittest failed (1)");
        }
        
        
        testLine = SingleLineDetector.createFromFloatPositions(new Vector2d<Float>(1.0f, 2.0f), new Vector2d<Float>(2.0f, 2.0f));
        point = new Vector2d<Float>(2.0f, 1.0f);
        
        projectedPoint = testLine.projectPointOntoLine(point);
        
        if( projectedPoint.x < 2.0f + 0.01f && projectedPoint.x > 2.0f - 0.01f && projectedPoint.y < 2.0f + 0.01f && projectedPoint.y > 2.0f - 0.01f ) 
        {
            // all fine
        }
        else
        {
            throw new RuntimeException("Unittest failed (1)");
        }
        
        testLine = SingleLineDetector.createFromFloatPositions(new Vector2d<Float>(0.0f, 1.0f), new Vector2d<Float>(1.0f, 0.0f));
        point = new Vector2d<Float>(2.0f, 1.0f);
        
        projectedPoint = testLine.projectPointOntoLine(point);
        
        
        int x = 0;
    }

    public Vector2d<Float> getPositionOfEndpoint(int index)
    {
        Assert.Assert(index == 0 || index == 1, "index must be 0 or 1");
        
        if( index == 0 )
        {
            return aFloat;
        }
        else
        {
            return bFloat;
        }
    }
    
    // TODO< figure out if it is the middle >
    //  for this we need to restructure the function that it does
    //  * if the line is not a singularity
    //    -> compare x value if its before the begin or after the end, or in the middle
    //  * if the line is singularity we have to do the same with the y axis
    public Intersection.IntersectionPartner.EnumIntersectionEndpointType getIntersectionEndpoint(Vector2d<Float> point)
    {
        Vector2d<Float> diff;
        float distanceBegin, distanceEnd;
        
        diff = sub(getAProjected(), point);
        distanceBegin = ptrman.Datastructures.Vector2d.FloatHelper.getLength(diff);
        
        diff = sub(getBProjected(), point);
        distanceEnd = ptrman.Datastructures.Vector2d.FloatHelper.getLength(diff);
        
        if( distanceBegin < distanceEnd )
        {
            return Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN;
        }
        else
        {
            return Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
        }
    }
}
