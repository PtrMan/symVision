package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.bpsolver.HardParameters;
import ptrman.misc.Assert;

import java.util.List;

/**
 * tries to combine linedetectors
 * 
 */
public class ProcessH {
    public void process(List<RetinaPrimitive> workingDetectors) {
        // called low and high because the index low is always lower than high
        int iteratorLow, iteratorHigh;
        
        // TODO< this algorithm is simple, it is possible to optimize this >
        // TODO< a possible solution is to flag "deleted" elements in the input array and store the fused Detectors in a second array
        //       then after one iteration these two arrays get merged (without deleted elements), this repeats as long as elements are fused
        //     >
        for(;;) {
            boolean terminate;
            
            terminate = true;
            
            repeatSearch:
            for( iteratorLow = 0; iteratorLow < workingDetectors.size(); iteratorLow++ ) {
                for( iteratorHigh = iteratorLow+1; iteratorHigh < workingDetectors.size(); iteratorHigh++ ) {
                    SingleLineDetector detectorLow;
                    SingleLineDetector detectorHigh;
                    
                    Assert.Assert(workingDetectors.get(iteratorLow).type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                    Assert.Assert(workingDetectors.get(iteratorHigh).type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                    
                    detectorLow = workingDetectors.get(iteratorLow).line;
                    detectorHigh = workingDetectors.get(iteratorHigh).line;
                    
                    if( canDetectorsBeFusedOverlap(detectorLow, detectorHigh) ) {
                        SingleLineDetector fusedLineDetector;

                        // fuse
                        fusedLineDetector = fuseLineDetectorsOverlap(detectorLow, detectorHigh);

                        // NOTE< order is important >
                        workingDetectors.remove(iteratorHigh);
                        workingDetectors.remove(iteratorLow);

                        workingDetectors.add(RetinaPrimitive.makeLine(fusedLineDetector));

                        // we need to repeat the search because we changed the array
                        terminate = false;
                        break repeatSearch;
                    }
                    else if( canDetectorsBeFusedInside(detectorLow, detectorHigh) ) {
                        SingleLineDetector fusedLineDetector;

                        // fuse
                        fusedLineDetector = fuseLineDetectorsInside(detectorLow, detectorHigh);

                        // NOTE< order is important >
                        workingDetectors.remove(iteratorHigh);
                        workingDetectors.remove(iteratorLow);

                        workingDetectors.add(RetinaPrimitive.makeLine(fusedLineDetector));

                        // we need to repeat the search because we changed the array
                        terminate = false;
                        break repeatSearch;
                    }
                }
            }
            
            if( terminate ) {
                break;
            }
        }
    }
    
    // overlap case
    private static boolean canDetectorsBeFusedOverlap(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        // TODO< vertical special case >

        SingleLineDetector inplaceDetectorA, inplaceDetectorB;
        
        ArrayRealVector projectedABegin = detectorA.getAProjected();
        ArrayRealVector projectedAEnd = detectorA.getBProjected();
        ArrayRealVector projectedBBegin = detectorB.getAProjected();
        ArrayRealVector projectedBEnd = detectorB.getBProjected();
        
        inplaceDetectorA = detectorA;
        inplaceDetectorB = detectorB;
        
        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        if( projectedBBegin.getDataRef()[0] < projectedABegin.getDataRef()[0] ) {
            ArrayRealVector tempBegin, tempEnd;
            SingleLineDetector tempDetector;
            
            
            tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            projectedBBegin = tempBegin;
            
            tempEnd = projectedAEnd;
            projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;
            
            
            tempDetector = inplaceDetectorA;
            inplaceDetectorA = inplaceDetectorB;
            inplaceDetectorB = tempDetector;
        }
        
        if( vectorXBetweenInclusive(projectedABegin, projectedAEnd, projectedBBegin) && vectorXBetweenInclusive(projectedBBegin, projectedBEnd, projectedAEnd) )
        {
        }
        else {
            return false;
        }
        
        // projecting the points on the other line and measue the distance
        
        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedBBegin, inplaceDetectorA) ) {
            return false;
        }
        
        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedAEnd, inplaceDetectorB) ) {
            return false;
        }
        
        return true;
    }
    
    // fusing for overlap case
    private static SingleLineDetector fuseLineDetectorsOverlap(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        // TODO< vertical special case >
        
        SingleLineDetector fusedLineDetector;
        
        // we fuse them with taking the lowest begin-x as the begin and the other as the end
        
        ArrayRealVector projectedABegin = detectorA.getAProjected();
        ArrayRealVector projectedAEnd = detectorA.getBProjected();
        ArrayRealVector projectedBBegin = detectorB.getAProjected();
        ArrayRealVector projectedBEnd = detectorB.getBProjected();
        
        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        // TODO BUG FIXME< the variables after the switching are not used >
        if( projectedBBegin.getDataRef()[0] < projectedABegin.getDataRef()[0] ) {
            ArrayRealVector tempBegin, tempEnd;
            
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
    private static boolean canDetectorsBeFusedInside(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        // TODO< vertical special case >
        
        // which case?
        if( vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.a) && vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.b)  ) {
            // detectorB inside detectorA ?
            if( isProjectedPointOntoLineBelowDistanceLimit(detectorB.a, detectorA) && isProjectedPointOntoLineBelowDistanceLimit(detectorB.b, detectorA)  ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if( vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.a) && vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.b) ) {
            // detectorA inside detectorB ?
            if( isProjectedPointOntoLineBelowDistanceLimit(detectorA.a, detectorB) && isProjectedPointOntoLineBelowDistanceLimit(detectorA.b, detectorB)  ) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }
    
    // TODO fuseLineDetectorsInside
    private static SingleLineDetector fuseLineDetectorsInside(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        SingleLineDetector fusedLineDetector;
        
        // TODO< vertical special case >
        
        // which case?
        if( vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.a) && vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.b)  ) {
            // detectorB inside detectorA
            
            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorA.a, detectorA.b);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
        else {
            Assert.Assert(vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.a) && vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.b), "");
            
            // detectorA inside detectorB
            
            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorB.a, detectorB.b);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
    }
    
    /**
     * checks if the value.x is between min.x and max.x
     *  
     */
    private static boolean vectorXBetween(ArrayRealVector min, ArrayRealVector max, ArrayRealVector value) {
        return value.getDataRef()[0] > min.getDataRef()[0] && value.getDataRef()[0] < max.getDataRef()[0];
    }
    
    private static boolean vectorXBetweenInclusive(ArrayRealVector min, ArrayRealVector max, ArrayRealVector value) {
        return value.getDataRef()[0] >= min.getDataRef()[0] && value.getDataRef()[0] <= max.getDataRef()[0];
    }
    
    private static boolean isProjectedPointOntoLineBelowDistanceLimit(ArrayRealVector point, SingleLineDetector line) {
        ArrayRealVector projectedPoint = line.projectPointOntoLine(point);
        double distanceBetweenProjectedAndPoint = projectedPoint.getDistance(point);
        
        //System.out.println("line A (" + line.aFloat.x.toString() + "," + line.aFloat.y.toString() + ") B (" + line.bFloat.x.toString() + "," + line.bFloat.y.toString() + ")");
        //System.out.println("point (" + point.x.toString() + "," + point.y.toString() + ")");
        //System.out.println("projectedpoint (" + projectedPoint.x.toString() + "," + projectedPoint.y.toString() + ")");
        
        return distanceBetweenProjectedAndPoint < HardParameters.ProcessH.MAXDISTANCEFORCANDIDATEPOINT;
    }
}
