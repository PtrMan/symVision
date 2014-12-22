package RetinaLevel;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.sub;
import static Datastructures.Vector2d.FloatHelper.getLength;
import bpsolver.HardParameters;
import java.util.ArrayList;
import misc.Assert;

/**
 * tries to combine linedetectors
 * 
 */
public class ProcessH
{
    public void process(ArrayList<SingleLineDetector> workingDetectors)
    {
        // called low and high because the index low is always lower than high
        int iteratorLow, iteratorHigh;
        
        // TODO< this algorithm is simple, it is possible to optimize this >
        // TODO< a possible solution is to flag "deleted" elements in the input array and store the fused Detectors in a second array
        //       then after one iteration these two arrays get merged (without deleted elements), this repeats as long as elements are fused
        //     >
        for(;;)
        {
            boolean terminate;
            
            terminate = true;
            
            repeatSearch:
            for( iteratorLow = 0; iteratorLow < workingDetectors.size(); iteratorLow++ )
            {
                for( iteratorHigh = iteratorLow+1; iteratorHigh < workingDetectors.size(); iteratorHigh++ )
                {
                    SingleLineDetector detectorLow;
                    SingleLineDetector detectorHigh;

                    detectorLow = workingDetectors.get(iteratorLow);
                    detectorHigh = workingDetectors.get(iteratorHigh);

                    if( canDetectorsBeFusedOverlap(detectorLow, detectorHigh) )
                    {
                        SingleLineDetector fusedLineDetector;

                        // fuse
                        fusedLineDetector = fuseLineDetectorsOverlap(detectorLow, detectorHigh);

                        // NOTE< order is important >
                        workingDetectors.remove(iteratorHigh);
                        workingDetectors.remove(iteratorLow);

                        workingDetectors.add(fusedLineDetector);

                        // we need to repeat the search because we changed the array
                        terminate = false;
                        break repeatSearch;
                    }
                    else if( canDetectorsBeFusedInside(detectorLow, detectorHigh) )
                    {
                        SingleLineDetector fusedLineDetector;

                        // fuse
                        fusedLineDetector = fuseLineDetectorsInside(detectorLow, detectorHigh);

                        // NOTE< order is important >
                        workingDetectors.remove(iteratorHigh);
                        workingDetectors.remove(iteratorLow);

                        workingDetectors.add(fusedLineDetector);

                        // we need to repeat the search because we changed the array
                        terminate = false;
                        break repeatSearch;
                    }
                }
            }
            
            if( terminate )
            {
                break;
            }
        }
    }
    
    // overlap case
    private static boolean canDetectorsBeFusedOverlap(SingleLineDetector detectorA, SingleLineDetector detectorB)
    {
        // TODO< vertical special case >
        
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
        
        if( vectorXBetweenInclusive(projectedABegin, projectedAEnd, projectedBBegin) && vectorXBetweenInclusive(projectedBBegin, projectedBEnd, projectedAEnd) )
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
    
    // fusing for overlap case
    private static SingleLineDetector fuseLineDetectorsOverlap(SingleLineDetector detectorA, SingleLineDetector detectorB)
    {
        // TODO< vertical special case >
        
        Vector2d<Float> projectedABegin, projectedAEnd;
        Vector2d<Float> projectedBBegin, projectedBEnd;
        SingleLineDetector fusedLineDetector;
        
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
        
        fusedLineDetector = SingleLineDetector.createFromFloatPositions(projectedABegin, projectedBEnd);
        fusedLineDetector.resultOfCombination = true;
        return fusedLineDetector;
    }
    
    // inside case
    private static boolean canDetectorsBeFusedInside(SingleLineDetector detectorA, SingleLineDetector detectorB)
    {
        // TODO< vertical special case >
        
        // which case?
        if( vectorXBetweenInclusive(detectorA.aFloat, detectorA.bFloat, detectorB.aFloat) && vectorXBetweenInclusive(detectorA.aFloat, detectorA.bFloat, detectorB.bFloat)  )
        {
            // detectorB inside detectorA ?
            if( isProjectedPointOntoLineBelowDistanceLimit(detectorB.aFloat, detectorA) && isProjectedPointOntoLineBelowDistanceLimit(detectorB.bFloat, detectorA)  )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if( vectorXBetweenInclusive(detectorB.aFloat, detectorB.bFloat, detectorA.aFloat) && vectorXBetweenInclusive(detectorB.aFloat, detectorB.bFloat, detectorA.bFloat) )
        {
            // detectorA inside detectorB ?
            if( isProjectedPointOntoLineBelowDistanceLimit(detectorA.aFloat, detectorB) && isProjectedPointOntoLineBelowDistanceLimit(detectorA.bFloat, detectorB)  )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    // TODO fuseLineDetectorsInside
    private static SingleLineDetector fuseLineDetectorsInside(SingleLineDetector detectorA, SingleLineDetector detectorB)
    {
        SingleLineDetector fusedLineDetector;
        
        // TODO< vertical special case >
        
        // which case?
        if( vectorXBetweenInclusive(detectorA.aFloat, detectorA.bFloat, detectorB.aFloat) && vectorXBetweenInclusive(detectorA.aFloat, detectorA.bFloat, detectorB.bFloat)  )
        {
            // detectorB inside detectorA
            
            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorA.aFloat, detectorA.bFloat);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
        else
        {
            Assert.Assert(vectorXBetweenInclusive(detectorB.aFloat, detectorB.bFloat, detectorA.aFloat) && vectorXBetweenInclusive(detectorB.aFloat, detectorB.bFloat, detectorA.bFloat), "");
            
            // detectorA inside detectorB
            
            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorB.aFloat, detectorB.bFloat);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
    }
    
    /**
     * checks if the value.x is between min.x and max.x
     *  
     */
    private static boolean vectorXBetween(Vector2d<Float> min, Vector2d<Float> max, Vector2d<Float> value)
    {
        return value.x > min.x && value.x < max.x;
    }
    
    private static boolean vectorXBetweenInclusive(Vector2d<Float> min, Vector2d<Float> max, Vector2d<Float> value)
    {
        return value.x >= min.x && value.x <= max.x;
    }
    
    private static boolean isProjectedPointOntoLineBelowDistanceLimit(Vector2d<Float> point, SingleLineDetector line)
    {
        Vector2d<Float> projectedPoint;
        float distanceBetweenProjectedAndPoint;
        
        projectedPoint = line.projectPointOntoLine(point);
        distanceBetweenProjectedAndPoint = getLength(sub(projectedPoint, point));
        
        return distanceBetweenProjectedAndPoint < HardParameters.ProcessH.MAXDISTANCEFORCANDIDATEPOINT;
    }
}
