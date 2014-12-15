package RetinaLevel;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.sub;
import static Datastructures.Vector2d.FloatHelper.getLength;
import bpsolver.HardParameters;
import java.util.ArrayList;

/**
 * tries to combine linedetectors
 * 
 */
public class ProcessH
{
    public void process(ArrayList<ProcessD.SingleLineDetector> workingDetectors)
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
                ProcessD.SingleLineDetector detectorLow;
                ProcessD.SingleLineDetector detectorHigh;
                
                detectorLow = workingDetectors.get(iteratorLow);
                detectorHigh = workingDetectors.get(iteratorHigh);
                
                if( canDetectorsBeFused(detectorLow, detectorHigh) )
                {
                    ProcessD.SingleLineDetector fusedLineDetector;
                    
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
    
    private static boolean canDetectorsBeFused(ProcessD.SingleLineDetector detectorA, ProcessD.SingleLineDetector detectorB)
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
    
    private static ProcessD.SingleLineDetector fuseLineDetectors(ProcessD.SingleLineDetector detectorA, ProcessD.SingleLineDetector detectorB)
    {
        Vector2d<Float> projectedABegin, projectedAEnd;
        Vector2d<Float> projectedBBegin, projectedBEnd;
        ProcessD.SingleLineDetector fusedLineDetector;
        
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
        
        fusedLineDetector = ProcessD.SingleLineDetector.createFromFloatPositions(projectedABegin, projectedBEnd);
        fusedLineDetector.resultOfCombination = true;
        return fusedLineDetector;
    }
    
    /**
     * checks if the value.x is between min.x and max.x
     *  
     */
    private static boolean vectorXBetween(Vector2d<Float> min, Vector2d<Float> max, Vector2d<Float> value)
    {
        return value.x > min.x && value.x < max.x;
    }
    
    private static boolean isProjectedPointOntoLineBelowDistanceLimit(Vector2d<Float> point, ProcessD.SingleLineDetector line)
    {
        Vector2d<Float> projectedPoint;
        float distanceBetweenProjectedAndPoint;
        
        projectedPoint = line.projectPointOntoLine(point);
        distanceBetweenProjectedAndPoint = getLength(sub(projectedPoint, point));
        
        return distanceBetweenProjectedAndPoint < HardParameters.ProcessH.MAXDISTANCEFORCANDIDATEPOINT;
    }
}
