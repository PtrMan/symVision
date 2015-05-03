package ptrman.levels.retina;

import ptrman.Datastructures.Vector2d;

import java.util.List;

import static java.lang.System.arraycopy;
import static ptrman.Datastructures.Vector2d.IntegerHelper.sub;

/**
 *
 * identifies if a point is a point of the endo or exosceleton
 */
public class ProcessC {
    private static class SampleWithDistance {
        public SampleWithDistance(ProcessA.Sample sample, float distance) {
            this.sample = sample;
            this.distance = distance;
            used = true;
        }
        
        public SampleWithDistance() {
        }
        
        public ProcessA.Sample sample;
        public float distance;
        
        public boolean used = false; // used for the sorted array
    }
    
    public void process(List<ProcessA.Sample> samples) {
        for( int outerI = 0; outerI < samples.size(); outerI++ ) {
            SampleWithDistance[] sortedArray = createSortedArray();

            ProcessA.Sample outerSample = samples.get(outerI);
            
            for( int innerI = 0; innerI < samples.size(); innerI++ ) {
                if( outerI == innerI ) {
                    continue;
                }

                ProcessA.Sample innerSample = samples.get(innerI);
                
                float distance = calculateDistanceBetweenSamples(outerSample, innerSample);
                putSampleWithDistanceIntoSortedArray(new SampleWithDistance(innerSample, distance), sortedArray);
            }
            
            
            if( noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(sortedArray, outerSample) ) {
                outerSample.type = ProcessA.Sample.EnumType.ENDOSCELETON;
            }
            else
            {
                outerSample.type = ProcessA.Sample.EnumType.EXOSCELETON;
            }
        }
    }
    
    /**
     *
     * \param sortedArray lower values are more left
     */
    private static void putSampleWithDistanceIntoSortedArray(SampleWithDistance newElement, SampleWithDistance[] sortedArray) {
        for( int i = 0; i < sortedArray.length; i++ ) {
            if( !sortedArray[i].used ) {
                // array element is not set, its save to set it to the newElement
                sortedArray[i] = newElement;
                
                return;
            }
            
            if( newElement.distance < sortedArray[i].distance ) {
                // shift one to the back
                arraycopy(
                    sortedArray,
                    i,
                    sortedArray,
                    i+1,
                    sortedArray.length-1-i
                );
                
                return;
            }
        }
    }
    
    private static SampleWithDistance[] createSortedArray() {
        SampleWithDistance[] result = new SampleWithDistance[8];
        
        for( int i = 0; i < result.length; i++ ) {
            result[i] = new SampleWithDistance();
        }
        
        return result;
    }
    
    private static float calculateDistanceBetweenSamples(ProcessA.Sample a, ProcessA.Sample b) {
        Vector2d<Integer> integerDiff;
        
        integerDiff = sub(a.position, b.position);
        
        return (float)Math.sqrt( (float)(integerDiff.x*integerDiff.x + integerDiff.y*integerDiff.y));
    }
    
    private static boolean noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(SampleWithDistance[] neightborArray, ProcessA.Sample compareSample) {
        int numberOfNeightborsWithAltitudeStrictlyGreaterThan = 0;
        
        for( int i = 0; i < neightborArray.length; i++ ) {
            if( !neightborArray[i].used ) {
                return true;
            }
            // else here
            
            if( neightborArray[i].sample.altitude > compareSample.altitude ) {
                numberOfNeightborsWithAltitudeStrictlyGreaterThan++;
                
                if( numberOfNeightborsWithAltitudeStrictlyGreaterThan > 2 ) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
