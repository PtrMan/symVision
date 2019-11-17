/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina;


import org.eclipse.collections.impl.list.mutable.FastList;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.levels.retina.helper.SpatialDrawer;
import ptrman.levels.retina.helper.SpatialListMap2d;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;

/**
 *
 * identifies if a point is a point of the endo- or exoskeleton
 */
public class ProcessC implements IProcess {

    private SpatialListMap2d<ProcessA.Sample> accelerationMap;

    final int maxSortedSamples = 8;

    private int gridsize;
    private Vector2d<Integer> imageSize;

    private final Random random = new Random();

    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<ProcessA.Sample> resultSamplesToProcessF;
    private ProcessConnector<ProcessA.Sample> resultSampleConnector;
    final FastList<SampleWithDistance> sortedSamples = new FastList();

    /** sort order: lowest distance first */
    private static class SampleWithDistance implements Comparable<SampleWithDistance> {


        public SampleWithDistance(ProcessA.Sample sample, double distance) {
            this.sample = sample;
            this.distance = distance;
            used = true;
        }
        

        public final ProcessA.Sample sample;
        public final double distance;

        public boolean used = false; // used for the sorted array

        @Override
        public int compareTo(SampleWithDistance o) {
            return Double.compare(distance, o.distance);
        }
    }

    public ProcessC() {

    }


    public void preSetupSet(final int gridsize) {
        this.gridsize = gridsize;
    }

    public void set(ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<ProcessA.Sample> resultSampleConnector, ProcessConnector<ProcessA.Sample> resultSamplesToProcessF) {
        if( inputSampleConnector.getWorkspace() == null ) {
            throw new RuntimeException("inputSampleConnector must be a workspace Connector!");
        }

        this.inputSampleConnector = inputSampleConnector;
        this.resultSamplesToProcessF = resultSamplesToProcessF;
        this.resultSampleConnector = resultSampleConnector;
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
    public void preProcessData() {

    }

    @Override
    public void processData() {
        processData(1f);
    }

    public void processData(float throttle) {

        // clean acceleration map
        accelerationMap.clear();

        // fill
        for( final ProcessA.Sample iterationSample : inputSampleConnector.getWorkspace()  ) {
            final Vector2d<Integer> sampleIntegerPosition = accelerationMap.getCellPositionOfIntegerPosition(arrayRealVectorToInteger(iterationSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN));
            List<ProcessA.Sample> samplesOfCell = accelerationMap.addAt(sampleIntegerPosition.x, sampleIntegerPosition.y, iterationSample);
        }


        int maxSortedSamples = (int)Math.ceil( this.maxSortedSamples * throttle );

        for( int outerI = 0; outerI < inputSampleConnector.getWorkspace().size(); outerI++ ) {



            //SampleWithDistance[] sortedArray = createSortedArray();

            ProcessA.Sample outerSample = inputSampleConnector.getWorkspace().get(outerI);

            int numberOfConsideredSamples = 0;

            int maxRadius = (int)java.lang.Math.sqrt(imageSize.x*imageSize.y);

            for( int currentRadius = 0; currentRadius < maxRadius; currentRadius++ ) {
                if( numberOfConsideredSamples >= 8 ) {
                    maxRadius = Math.min(currentRadius + 2, maxRadius);
                }

                List<Vector2d<Integer>> cellPositionsToScan;

                if( currentRadius == 0 ) {
                    cellPositionsToScan = new FastList<>();
                    cellPositionsToScan.add(accelerationMap.getCellPositionOfIntegerPosition(arrayRealVectorToInteger(outerSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN)));
                }
                else {
                    cellPositionsToScan = SpatialDrawer.getPositionsOfCellsOfCircleBound(accelerationMap.getCellPositionOfIntegerPosition(arrayRealVectorToInteger(outerSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN)), currentRadius, new Vector2d<>(accelerationMap.getWidth(), accelerationMap.getLength()));
                }

                for( final Vector2d<Integer> currentCellPosition : cellPositionsToScan ) {
                    final List<ProcessA.Sample> samplesOfCurrentCell = accelerationMap.readAt(currentCellPosition.xInt(), currentCellPosition.yInt());
                    if (samplesOfCurrentCell!=null) {

                        for (final ProcessA.Sample iterationSample : samplesOfCurrentCell) {
                            // we don't want to calculate it for the same sample
                            if (iterationSample.equals(outerSample)) {
                                continue;
                            }

                            final double distance = calculateDistanceBetweenSamples(outerSample, iterationSample);

                            numberOfConsideredSamples++;


                            if (sortedSamples.size() < maxSortedSamples || distance < sortedSamples.getLast().distance) {
                                SampleWithDistance sd = new SampleWithDistance(iterationSample, distance);

                                /** binary insertion sort: sort order: lowest distance first */
                                int index = Collections.binarySearch(sortedSamples, sd);
                                if (index < 0) {
                                    index = -(index + 1);
                                    if (index < maxSortedSamples) {
                                        sortedSamples.add(index, sd);
                                        while (sortedSamples.size() > maxSortedSamples)
                                            sortedSamples.remove(sortedSamples.size() - 1);
                                    }
                                }
                            }

                            //sortedSamples.add(sd);

//                            putSampleWithDistanceIntoSortedArray(, sortedArray);
//                            Arrays.sort(sortedArray, new Comparator<SampleWithDistance>() {
//                                @Override public int compare(SampleWithDistance a, SampleWithDistance b) {
//                                    return Double.compare(a.distance, b.distance);
//                                }
//                            });
                        }
                    }
                }
            }



            if( noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(sortedSamples, outerSample) ) {
                outerSample.type = ProcessA.Sample.EnumType.ENDOSCELETON;

                if( outerSample.altitude >= HardParameters.ProcessC.FILLEDREGIONALTITUDETHRESHOLD ) {
                    if( random.nextFloat() < HardParameters.ProcessC.FILLEDREGIONCANDIDATEPROPABILITY ) {
                        resultSamplesToProcessF.add(outerSample);
                    }
                }
            }
            else {
                outerSample.type = ProcessA.Sample.EnumType.EXOSCELETON;
            }

            resultSampleConnector.add(outerSample);

            sortedSamples.clear();
        }
    }

    @Override
    public void postProcessData() {

    }

//    /**
//     *
//     * \param sortedArray lower values are more left
//     */
//    private static void putSampleWithDistanceIntoSortedArray(SampleWithDistance newElement, SampleWithDistance[] sortedArray) {
//
//
//        for( int i = 0; i < sortedArray.length; i++ ) {
//            if( !sortedArray[i].used ) {
//                // array element is not set, its save to set it to the newElement
//                sortedArray[i] = newElement;
//                sortedArray[i].used = true;
//
//                return;
//            }
//
//            if( newElement.distance < sortedArray[i].distance ) {
//                // shift one to the back
//                arraycopy(
//                    sortedArray,
//                    i,
//                    sortedArray,
//                    i+1,
//                    sortedArray.length-1-i
//                );
//
//                // add
//                sortedArray[i] = newElement;
//                sortedArray[i].used = true;
//
//                return;
//            }
//        }
//
//
//    }

    
    private static double calculateDistanceBetweenSamples(ProcessA.Sample a, ProcessA.Sample b) {
        return a.position.getDistance(b.position);
    }
    
    private static boolean noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(Collection<SampleWithDistance> neightborArray, ProcessA.Sample compareSample) {
        int numberOfNeightborsWithAltitudeStrictlyGreaterThan = 0;

        for (SampleWithDistance s : neightborArray) {
            if( !s.used ) {
                return true;
            }
            // else here

            if( s.sample.altitude > compareSample.altitude ) {
                numberOfNeightborsWithAltitudeStrictlyGreaterThan++;

                if( numberOfNeightborsWithAltitudeStrictlyGreaterThan > 2 ) {
                    return false;
                }
            }
        }
        
        return true;
    }


}
