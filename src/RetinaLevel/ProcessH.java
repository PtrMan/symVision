package RetinaLevel;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.dot;
import static Datastructures.Vector2d.FloatHelper.getScaled;
import static Datastructures.Vector2d.FloatHelper.sub;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.getLength;
import bpsolver.Parameters;
import java.util.ArrayList;

/**
 * tries to combine linedetectors
 * 
 */
public class ProcessH
{
    public void process(ArrayList<ProcessD.LineDetector> workingDetectors)
    {
        // called low and high because the index low is always lower than high
        int iteratorLow, iteratorHigh;
        
        // TODO< this algorithm is simple, it is possible to optimize this >
        // TODO< a possible solution is to flag "deleted" elements in the input array and store the fused Detectors in a second array
        //       then after one iteration these two arrays get merged (without deleted elements), this repeats as long as elements are fused
        //     >
        repeatSearch:
        for( iteratorLow = 0; iteratorLow < workingDetectors.size(); iteratorLow++ )
        {
            for( iteratorHigh = iteratorLow+1; iteratorHigh < workingDetectors.size(); iteratorHigh++ )
            {
                ProcessD.LineDetector detectorLow;
                ProcessD.LineDetector detectorHigh;
                
                detectorLow = workingDetectors.get(iteratorLow);
                detectorHigh = workingDetectors.get(iteratorHigh);
                
                if( canDetectorsBeFused(detectorLow, detectorHigh) )
                {
                    ProcessD.LineDetector fusedLineDetector;
                    
                    // fuse
                    fusedLineDetector = fuseLineDetectors(detectorLow, detectorHigh);
                    
                    // NOTE< order is important >
                    workingDetectors.remove(iteratorHigh);
                    workingDetectors.remove(iteratorLow);
                    
                    workingDetectors.add(fusedLineDetector);
                    
                    // we need to repeat the search because we changed the array
                    break repeatSearch;
                }
            }
        }
    }
    
    private static boolean canDetectorsBeFused(ProcessD.LineDetector detectorA, ProcessD.LineDetector detectorB)
    {
        Vector2d<Float> projectedABegin, projectedAEnd;
        Vector2d<Float> projectedBBegin, projectedBEnd;
        
        projectedABegin = detectorA.getAProjected();
        projectedAEnd = detectorA.getBProjected();
        projectedBBegin = detectorB.getAProjected();
        projectedBEnd = detectorB.getBProjected();
        
        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        if( projectedBBegin.x < projectedABegin.x )
        {
            Vector2d<Float> tempBegin, tempEnd;
            
            tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            projectedBBegin = tempBegin;
            
            tempEnd = projectedAEnd;
            projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;
        }
        
        if( vectorXBetween(projectedABegin, projectedAEnd, projectedBBegin) && vectorXBetween(projectedBBegin, projectedBEnd, projectedAEnd) )
        {
        }
        else
        {
            return false;
        }
        
        // projecting the points on the other line and measue the distance
        
        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedBBegin, detectorA) )
        {
            return false;
        }
        
        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedAEnd, detectorB) )
        {
            return false;
        }
        
        return true;
    }
    
    private static ProcessD.LineDetector fuseLineDetectors(ProcessD.LineDetector detectorA, ProcessD.LineDetector detectorB)
    {
        Vector2d<Float> projectedABegin, projectedAEnd;
        Vector2d<Float> projectedBBegin, projectedBEnd;
        
        // we fuse them with taking the lowest begin-x as the begin and the other as the end
        
        projectedABegin = detectorA.getAProjected();
        projectedAEnd = detectorA.getBProjected();
        projectedBBegin = detectorB.getAProjected();
        projectedBEnd = detectorB.getBProjected();
        
        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        if( projectedBBegin.x < projectedABegin.x )
        {
            Vector2d<Float> tempBegin, tempEnd;
            
            tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            projectedBBegin = tempBegin;
            
            tempEnd = projectedAEnd;
            projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;
        }
        
        return ProcessD.LineDetector.createFromFloatPositions(projectedABegin, projectedBEnd, null);
    }
    
    /**
     * checks if the value.x is between min.x and max.x
     *  
     */
    private static boolean vectorXBetween(Vector2d<Float> min, Vector2d<Float> max, Vector2d<Float> value)
    {
        return value.x > min.x && value.x < max.x;
    }
    
    private static boolean isProjectedPointOntoLineBelowDistanceLimit(Vector2d<Float> point, ProcessD.LineDetector line)
    {
        Vector2d<Float> projectedPoint;
        float distanceBetweenProjectedAndPoint;
        
        projectedPoint = projectPointOntoLine(point, line);
        distanceBetweenProjectedAndPoint = getLength(sub(projectedPoint, point));
        
        return distanceBetweenProjectedAndPoint < Parameters.ProcessH.MAXDISTANCEFORCANDIDATEPOINT;
    }
    
    private static Vector2d<Float> projectPointOntoLine(Vector2d<Float> point, ProcessD.LineDetector line)
    {
        Vector2d<Float> lineDirection;
        Vector2d<Float> diffFromAToPoint;
        float dotResult;
        
        lineDirection = line.getProjectedNormalizedDirection();
        diffFromAToPoint = sub(point, line.getAProjected());
        dotResult = dot(lineDirection, diffFromAToPoint);
        
        return add(line.getAProjected(), getScaled(lineDirection, dotResult));
    }
}
