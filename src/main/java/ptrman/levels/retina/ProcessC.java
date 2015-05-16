package ptrman.levels.retina;

import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.levels.retina.helper.SpatialCircleDrawer;
import ptrman.levels.retina.helper.SpatialListMap2d;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static java.lang.System.arraycopy;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;

/**
 *
 * identifies if a point is a point of the endo or exosceleton
 */
public class ProcessC implements IProcess {



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

    // TODO< split parameters into two methods somehow >
    public void set(List<ProcessA.Sample> samples) {
        this.samples = samples;
    }

    public void recalculate() {
        // clean acceleration map
        accelerationMap.clean();

        // fill
        for( final ProcessA.Sample iterationSample : samples ) {
            final Vector2d<Integer> sampleIntegerPosition = accelerationMap.getCellPositionOfIntegerPosition(arrayRealVectorToInteger(iterationSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN));
            List<ProcessA.Sample> samples = accelerationMap.readAt(sampleIntegerPosition.x, sampleIntegerPosition.y);
            samples.add(iterationSample);
        }
    }


    public void preSetupSet(final int gridsize, Queue<ProcessA.Sample> resultSamplesQueue) {
        this.gridsize = gridsize;
        this.resultSamplesQueue = resultSamplesQueue;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {
        Assert.Assert(imageSize != null, "imagesize is null");

        accelerationMap = new SpatialListMap2d<>(imageSize, gridsize);
    }

    @Override
    public void processData() {
        for( int outerI = 0; outerI < samples.size(); outerI++ ) {
            SampleWithDistance[] sortedArray = createSortedArray();

            ProcessA.Sample outerSample = samples.get(outerI);

            int numberOfConsideredSamples = 0;

            int maxRadius = (int)java.lang.Math.sqrt(imageSize.x*imageSize.y);

            for( int currentRadius = 0; currentRadius < maxRadius; currentRadius++ ) {
                if( numberOfConsideredSamples >= 8 ) {
                    maxRadius = Math.min(currentRadius + 2, maxRadius);
                }

                List<Vector2d<Integer>> cellPositionsToScan;

                if( currentRadius == 0 ) {
                    cellPositionsToScan = new ArrayList<>();
                    cellPositionsToScan.add(accelerationMap.getCellPositionOfIntegerPosition(arrayRealVectorToInteger(outerSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN)));
                }
                else {
                    cellPositionsToScan = SpatialCircleDrawer.getPositionsOfCellsOfCircle(accelerationMap.getCellPositionOfIntegerPosition(arrayRealVectorToInteger(outerSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN)), currentRadius, new Vector2d<>(accelerationMap.getWidth(), accelerationMap.getLength()));
                }

                int debug = 0;

                for( final Vector2d<Integer> currentCellPosition : cellPositionsToScan ) {
                    final List<ProcessA.Sample> samplesOfCurrentCell = accelerationMap.readAt(currentCellPosition.x, currentCellPosition.y);

                    for( final ProcessA.Sample iterationSample : samplesOfCurrentCell ) {
                        // we don't want to calculate it for the same sample
                        if( iterationSample.equals(outerSample) ) {
                            continue;
                        }

                        final double distance = calculateDistanceBetweenSamples(outerSample, iterationSample);

                        numberOfConsideredSamples++;
                        putSampleWithDistanceIntoSortedArray(new SampleWithDistance(iterationSample, distance), sortedArray);
                    }
                }
            }
            
            if( noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(sortedArray, outerSample) ) {
                outerSample.type = ProcessA.Sample.EnumType.ENDOSCELETON;

                if( outerSample.altitude >= HardParameters.ProcessC.FILLEDREGIONALTITUDETHRESHOLD ) {
                    if( random.nextFloat() < HardParameters.ProcessC.FILLEDREGIONCANDIDATEPROPABILITY ) {
                        queueToProcessF.add(outerSample);
                    }
                }
            }
            else {
                outerSample.type = ProcessA.Sample.EnumType.EXOSCELETON;
            }

            resultSamplesQueue.add(outerSample);
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

    private final Queue<ProcessA.Sample> queueToProcessF;
    private Queue<ProcessA.Sample> resultSamplesQueue;


    private SpatialListMap2d<ProcessA.Sample> accelerationMap;

    private int gridsize;
    private Vector2d<Integer> imageSize;
    private List<ProcessA.Sample> samples;

    private Random random = new Random();
}
