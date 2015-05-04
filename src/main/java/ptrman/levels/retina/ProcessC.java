package ptrman.levels.retina;

import java.util.List;
import java.util.Queue;

import static java.lang.System.arraycopy;

/**
 *
 * identifies if a point is a point of the endo or exosceleton
 */
public class ProcessC {
    private static class SampleWithDistance {
        public SampleWithDistance(ProcessA.Sample sample, double distance) {
            this.sample = sample;
            this.distance = distance;
            used = true;
        }
        
        public SampleWithDistance() {
        }
        
        public ProcessA.Sample sample;
        public double distance;
        
        public boolean used = false; // used for the sorted array
    }

    public ProcessC(Queue<ProcessA.Sample> queueToProcessF) {
        this.queueToProcessF = queueToProcessF;
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
                
                double distance = calculateDistanceBetweenSamples(outerSample, innerSample);

                putSampleWithDistanceIntoSortedArray(new SampleWithDistance(innerSample, distance), sortedArray);
            }
            
            
            if( noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(sortedArray, outerSample) ) {
                outerSample.type = ProcessA.Sample.EnumType.EXOSCELETON;
            }
            else {
                outerSample.type = ProcessA.Sample.EnumType.ENDOSCELETON;
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
                sortedArray[i].used = true;
                
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

                // add
                sortedArray[i] = newElement;
                sortedArray[i].used = true;
                
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
    
    private static double calculateDistanceBetweenSamples(ProcessA.Sample a, ProcessA.Sample b) {
        return a.position.getDistance(b.position);
    }
    
    private static boolean noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(SampleWithDistance[] neightborArray, ProcessA.Sample compareSample) {
        int numberOfNeightborsWithAltitudeStrictlyGreaterThan = 0;

        System.out.println("---");

        for( int i = 0; i < neightborArray.length; i++ ) {
            if( !neightborArray[i].used ) {
                return true;
            }
            // else here

            System.out.println("i = " + (""+i));

            if( neightborArray[i].sample.altitude < compareSample.altitude ) {
                numberOfNeightborsWithAltitudeStrictlyGreaterThan++;

                System.out.println("numberOfNeightborsWithAltitudeStrictlyGreaterThan " + (""+numberOfNeightborsWithAltitudeStrictlyGreaterThan));

                
                if( numberOfNeightborsWithAltitudeStrictlyGreaterThan > 2 ) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private final Queue<ProcessA.Sample> queueToProcessF;
}
