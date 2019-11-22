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


import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.list.mutable.FastList;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.levels.retina.helper.SpatialDrawer;
import ptrman.levels.retina.helper.SpatialListMap2d;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 *
 * identifies if a point is a point of the endo- or exoskeleton
 */
public class ProcessC implements IProcess {

    private SpatialListMap2d<ProcessA.Sample> accelerationMap;

    public final int maxSortedSamples = 8;

    public int gridsize = 8;
    private Vector2d<Integer> imageSize;

    private final Random random = new Random();

    public ProcessConnector<ProcessA.Sample> inputSampleConnector;
    public ProcessConnector<ProcessA.Sample> resultSamplesToProcessF;
    public ProcessConnector<ProcessA.Sample> resultSampleConnector;
    final FastList<SampleWithDistance> sortedSamples = new FastList();

    /** sort order: lowest distance first */
    private static class SampleWithDistance implements Comparable<SampleWithDistance> {


        public SampleWithDistance(final ProcessA.Sample sample, final double distance) {
            this.sample = sample;
            this.distance = distance;
            used = true;
        }
        

        public final ProcessA.Sample sample;
        public final double distance;

        public boolean used = false; // used for the sorted array

        @Override
        public int compareTo(final SampleWithDistance o) {
            return Double.compare(distance, o.distance);
        }
    }

    public ProcessC() {

    }

    public void set(final ProcessConnector<ProcessA.Sample> inputSampleConnector, final ProcessConnector<ProcessA.Sample> resultSampleConnector, final ProcessConnector<ProcessA.Sample> resultSamplesToProcessF) {
        assert inputSampleConnector.getWorkspace() != null : "inputSampleConnector must be a workspace Connector!";

        this.inputSampleConnector = inputSampleConnector;
        this.resultSamplesToProcessF = resultSamplesToProcessF;
        this.resultSampleConnector = resultSampleConnector;
    }

    @Override
    public void setImageSize(final Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {
        assert imageSize != null : "ASSERT: " + "imagesize is null";

        accelerationMap = new SpatialListMap2d<>(imageSize, gridsize);
    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void processData() {
        processData(1f);
    }

    public void processData(final float throttle) {

        // clean acceleration map
        accelerationMap.clear();

        // fill
        for( final var iterationSample : inputSampleConnector.getWorkspace()  ) {
            final var p = accelerationMap.getCellPositionOfIntegerPosition(iterationSample.position);
            final var samplesOfCell = accelerationMap.addAt(p.getOne(), p.getTwo(), iterationSample);
        }


        final var maxSortedSamples = (int)Math.ceil( this.maxSortedSamples * throttle );

        for(var outerI = 0; outerI < inputSampleConnector.getWorkspace().size(); outerI++ ) {



            //SampleWithDistance[] sortedArray = createSortedArray();

            final var outerSample = inputSampleConnector.getWorkspace().get(outerI);

            var numberOfConsideredSamples = 0;

            var maxRadius = (int)java.lang.Math.sqrt(imageSize.x*imageSize.y);

            for(var currentRadius = 0; currentRadius < maxRadius; currentRadius++ ) {
                if( numberOfConsideredSamples >= 8 ) maxRadius = Math.min(currentRadius + 2, maxRadius);

                final List<IntIntPair> cellPositionsToScan;

                if( currentRadius == 0 ) {
                    cellPositionsToScan = new FastList<>(1);
                    cellPositionsToScan.add(accelerationMap.getCellPositionOfIntegerPosition(outerSample.position));
                }
                else
                    cellPositionsToScan = SpatialDrawer.getPositionsOfCellsOfCircleBound(accelerationMap.getCellPositionOfIntegerPosition(outerSample.position), currentRadius, pair(accelerationMap.getWidth(), accelerationMap.getLength()));

                for( final var currentCellPosition : cellPositionsToScan ) {
                    final var samplesOfCurrentCell = accelerationMap.readAt(currentCellPosition.getOne(), currentCellPosition.getTwo());
                    if (samplesOfCurrentCell!=null) for (final var iterationSample : samplesOfCurrentCell) {
                        // we don't want to calculate it for the same sample
                        if (iterationSample.equals(outerSample)) continue;

                        final var distance = calculateDistanceBetweenSamples(outerSample, iterationSample);

                        numberOfConsideredSamples++;


                        if (sortedSamples.size() < maxSortedSamples || distance < sortedSamples.getLast().distance) {
                            final var sd = new SampleWithDistance(iterationSample, distance);

                            /** binary insertion sort: sort order: lowest distance first */
                            var index = Collections.binarySearch(sortedSamples, sd);
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



            if( noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(sortedSamples, outerSample) ) {
                outerSample.type = ProcessA.Sample.EnumType.ENDOSCELETON;

                if( outerSample.altitude >= HardParameters.ProcessC.FILLEDREGIONALTITUDETHRESHOLD )
                    if (random.nextFloat() < HardParameters.ProcessC.FILLEDREGIONCANDIDATEPROPABILITY)
                        resultSamplesToProcessF.add(outerSample);
            }
            else outerSample.type = ProcessA.Sample.EnumType.EXOSCELETON;

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

    
    private static double calculateDistanceBetweenSamples(final ProcessA.Sample a, final ProcessA.Sample b) {
        if (a==b) return 0;

        //a.position.getDistance(b.position);
        final double dx = b.position.getOne() - a.position.getOne();
        final double dy = b.position.getTwo() - a.position.getTwo();
        return Math.sqrt(dx*dx+dy*dy);
    }
    
    private static boolean noMoreThanTwoNeightborsWithAltidudeStrictlyGreaterThan(final Iterable<SampleWithDistance> neightborArray, final ProcessA.Sample compareSample) {
        var numberOfNeightborsWithAltitudeStrictlyGreaterThan = 0;

        for (final var s : neightborArray) {
            if( !s.used ) return true;
            // else here

            if( s.sample.altitude > compareSample.altitude ) {
                numberOfNeightborsWithAltitudeStrictlyGreaterThan++;

                if( numberOfNeightborsWithAltitudeStrictlyGreaterThan > 2 ) return false;
            }
        }
        
        return true;
    }


}
