package RetinaLevel;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.dot;
import static Datastructures.Vector2d.FloatHelper.getScaled;
import static Datastructures.Vector2d.FloatHelper.sub;
import java.util.ArrayList;

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
    
    public ArrayList<LineIntersection> intersections = new ArrayList<>();
    
    public boolean isBetweenOrginalStartAndEnd(Vector2d<Float> position) {
        Vector2d<Float> diffAB, diffABnormalizd, diffAPosition;
        float length;
        float dotProduct;

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
        return point.x >= getAProjected().x && point.x <= getBProjected().x;
    }

    public float getN()
    {
        // TODO< m = inf special handling >
        return aFloat.y - aFloat.x*getM();
    }

    public float getM()
    {
        Vector2d<Float> diff;

        diff = sub(bFloat, aFloat);
        return diff.y/diff.x;
    }

    public float getLength() {
        Vector2d<Float> diff;
        
        diff = sub(aFloat, bFloat);
        
        return Datastructures.Vector2d.FloatHelper.getLength(diff);
    }
    
    public static float getAngleBetween(SingleLineDetector a, SingleLineDetector b)
    {
        Vector2d<Float> aNormalizedDirection, bNormalizedDirection;
        float dotResult;
        float angleInRad;
        float angleInDegree;
        
        aNormalizedDirection = a.getNormalizedDirection();
        bNormalizedDirection = b.getNormalizedDirection();
        
        dotResult = dot(aNormalizedDirection, bNormalizedDirection);
        angleInRad = (float)Math.acos(dotResult);
        angleInDegree = angleInRad * (360.0f/(2.0f*(float)Math.PI));
        
        if( angleInDegree > 90.0f )
        {
            angleInDegree = 180.0f - angleInDegree;
        }
        
        return angleInDegree;
    }

    float getTangentM()
    {
        float m;
        
        m = getM();
        
        return -1.0f/m;
    }
    
    public static Vector2d<Float> intersectLineDetectors(SingleLineDetector lineA, SingleLineDetector lineB)
    {
        return SingleLineDetector.intersectLinesMN(lineA.getM(), lineA.getN(), lineB.getM(), lineB.getN());
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
}
