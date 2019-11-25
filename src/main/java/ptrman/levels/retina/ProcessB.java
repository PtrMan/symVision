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
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import ptrman.Datastructures.*;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;


/**
 *
 * computation of altitude of points
 */
public class ProcessB extends AbstractProcessB {
    private SpatialAcceleratedMap2d spatialAcceleratedMap2d;
    private FastBooleanMap2d map;

    private int counterCellPositiveCandidates;
    private int counterCellCandidates;
    public IMap2d<Boolean> inputMap;
    public Queue<ProcessA.Sample> inputSampleConnector;
    public Queue<ProcessA.Sample> outputSampleConnector;

    @Override
    public void set(IMap2d<Boolean> map, Queue<ProcessA.Sample> inputSampleConnector, Queue<ProcessA.Sample> outputSampleConnector) {
        this.inputMap = map;
        this.inputSampleConnector = inputSampleConnector;
        this.outputSampleConnector = outputSampleConnector;
    }

    @Override
    public void setup() {
    }

    @Override
    public void preProcessData() {
    }

    /**
     *
     * we use the whole image, in Phaeaco he worked with the incomplete image with the guiding of processA, this is not implemented that way
     */
    @Override
    public void processData() {

        final int MAXRADIUS = (int)Math.ceil( Math.sqrt( imageSize.x*imageSize.x + imageSize.y*imageSize.y) ); // (int)Math.sqrt(squaredDistance(new double[]{(double)imageSize.x, (double)imageSize.y}));

        final int GRIDSIZE_FOR_SPATIALACCELERATEDMAP2D = 8;

        counterCellPositiveCandidates = 0;
        counterCellCandidates = 0;

        this.map = convertMapToFastBooleanMap2d(inputMap);

        spatialAcceleratedMap2d = new SpatialAcceleratedMap2d(map, GRIDSIZE_FOR_SPATIALACCELERATEDMAP2D);
        spatialAcceleratedMap2d.recalculateGridCellStateMap();
        
        for( ProcessA.Sample iterationSample : inputSampleConnector) {

            Tuple2<IntIntPair, Double> nearestResult = findNearestPositionWhereMapIs(false, iterationSample.position, map, MAXRADIUS);
            if( nearestResult == null ) {
                iterationSample.altitude =
                    //TODO CHECK
                    //((MAXRADIUS+1)*2)*((MAXRADIUS+1)*2);
                    Math.sqrt( Math.pow(((MAXRADIUS+1)*2),2) + Math.pow(((MAXRADIUS+1)*2),2 ));
            } else {
                // create a new sample to the output connector
                ProcessA.Sample outputSample = iterationSample.getClone();
                outputSample.altitude = nearestResult.e1;
                outputSampleConnector.add(outputSample);
            }
        }

        System.out.println("cell acceleration (positive cases): " + ((float) counterCellPositiveCandidates / (float) counterCellCandidates) * 100.0f + "%" );
    }

    @Override
    public void postProcessData() {
    }

    private static FastBooleanMap2d convertMapToFastBooleanMap2d(IMap2d<Boolean> map) {
        final int roundUpWidth = 64 + map.getWidth() - (map.getWidth() % 64);
        FastBooleanMap2d fastMap = new FastBooleanMap2d(roundUpWidth, map.getLength());

        for( int y = 0; y < map.getLength(); y++ ) {
            for( int x = 0; x < map.getWidth(); x++ ) {
                fastMap.setAt(x, y, map.readAt(x, y));
            }
        }

        return fastMap;
    }

    // TODO< move into external function >
    // TODO< provide a version which doesn't need a maxradius (we need only that version) >
    /**
     * 
     * \return null if no point could be found in the radius 
     */
    private Tuple2<IntIntPair, Double> findNearestPositionWhereMapIs(boolean value, IntIntPair position, IMap2d<Boolean> image, int radius) {
        /* debug
        if( position.x > 80 && position.x < 90 && position.y > 60 && position.y < 70 ) {
            int x = 0;
        }
        */

        IMap2d<Boolean> debugMap = new Map2d<>(spatialAcceleratedMap2d.getSize().x, spatialAcceleratedMap2d.getSize().y);
        for( int y = 0; y < debugMap.getLength(); y++ ) {
            for( int x = 0; x < debugMap.getWidth(); x++ ) {
                debugMap.setAt(x, y, false);
            }
        }

//        final ArrayRealVector positionReal = ptrman.math.ArrayRealVectorHelper.integerToArrayRealVector(position);

        final IntIntPair gridCenterPosition = spatialAcceleratedMap2d.getGridPositionOfPosition(position);

        float gridMaxSearchRadius = 2f + ((float)radius) / spatialAcceleratedMap2d.getGridsize();

        // set this to int.max when the radius is not limited

        final IntIntPair[] nearestPixelCandidate = new IntIntPair[]{null};
        final double[] nearestPixelCandidateDistanceSquared = {Double.MAX_VALUE};

        int radiusToScan = (int) Math.ceil(gridMaxSearchRadius);

        /** grid cells to scan TODO use a LongHashSet storing the int pair into 64-bit long, or 32-bit int */
        Collection<IntIntPair> toScan = new UnifiedSet(0); //(int)Math.ceil(radius * radius * Math.PI));

        Function<IntIntPair, Stream<? extends IntIntPair>> getPositionsOfCandidatePixelsOfCellWhereFalse = this::getPositionsOfCandidatePixelsOfCellWhereFalse;

        for( int currentGridRadius = 0; currentGridRadius < radiusToScan; currentGridRadius++ ) {

            // if we are at the center we need to scan only the center

            toScan.clear();

            if( currentGridRadius == 0 )
                toScan.add(gridCenterPosition);
            else {
                spatialAcceleratedMap2d.getGridLocationsWithNegativeDirectionOfGridRadius(gridCenterPosition, currentGridRadius, toScan);
            }

            /*
            // debugging
            System.out.println("---");
            for( final Vector2d<Integer> iterationGridCellPosition : gridCellsToScan ) {
                //System.out.println("gridCell position " + Integer.toString(iterationGridCellPosition.x) + " " + Integer.toString(iterationGridCellPosition.y));

                debugMap.setAt(iterationGridCellPosition.x, iterationGridCellPosition.y, true);
            }

            // debug display map
            for( int y = 0; y < debugMap.getLength(); y++ ) {
                for( int x = 0; x < debugMap.getWidth(); x++ ) {
                    boolean valueRead = debugMap.readAt(x, y);

                    if( valueRead ) {
                        System.out.print("x");
                    }
                    else {
                        System.out.print(".");
                    }

                }

                System.out.println();
            }
             */

            // use acceleration map and filter out the gridcells we don't need to scan
            toScan.removeIf(c -> !spatialAcceleratedMap2d.canValueBeFoundInCell(c, value));

            // statistics
            int scanSize = toScan.size();
            counterCellCandidates += scanSize;
            counterCellPositiveCandidates += scanSize;

            // do this because we need to scan the next radius too
            radiusToScan = java.lang.Math.min(radiusToScan, currentGridRadius+1+1);

            // pixel scan logic
            //TODO use .collect or something

            toScan.stream().flatMap(getPositionsOfCandidatePixelsOfCellWhereFalse).forEach(i -> {
                final double currentDistanceSquared = ArrayRealVectorHelper.diffDotProduct(position, i);
                if( currentDistanceSquared < nearestPixelCandidateDistanceSquared[0]) {
                    nearestPixelCandidateDistanceSquared[0] = currentDistanceSquared;
                    nearestPixelCandidate[0] = i;
                }
            });
        }

        return nearestPixelCandidate[0] == null ? null :
            new Tuple2<>(nearestPixelCandidate[0], Math.sqrt(nearestPixelCandidateDistanceSquared[0]));
    }

    private Stream<IntIntPair> getPositionsOfCandidatePixelsOfCellWhereFalse(final IntIntPair cellPosition) {

        final int gridsize = spatialAcceleratedMap2d.getGridsize();

        Assert.Assert(gridsize == 8, "only implemented for gridsize = 8");

        int px = cellPosition.getOne();
        int x0 = px * gridsize;
        int x1 = (px + 1) * gridsize;

        int py = cellPosition.getTwo();
        return IntStream.range(py * gridsize, (py + 1) * gridsize).mapToObj((int y) ->
            IntStream.range(x0, x1)
                .filter((int x) -> !map.readAt(x, y))
                .mapToObj((int x) -> pair(x,y))
        ).flatMap(x -> x);

//        int h = (cellPosition.y + 1) * gridsize;
//        int w = (cellPosition.x + 1) * gridsize;
//
//        for(int y = cellPosition.y * gridsize; y < h; y++ ) {
//
//            if( map.readByteAtInt(cellPosition.x * gridsize, y) != 0xff ) {
//                for(int x = cellPosition.x * gridsize; x < w; x++ ) {
//                    if (!map.readAt(x, y)) {
//                        result.add(new Vector2d<>(x, y));
//                    }
//                }
//            }
//        }
    }


    private List<Vector2d<Integer>> getPositionsOfCandidatePixelsOfCell(final Vector2d<Integer> cellPosition, final boolean value) {
        List<Vector2d<Integer>> result = new ArrayList<>();

        final int gridsize = spatialAcceleratedMap2d.getGridsize();

        for( int y = cellPosition.y * gridsize; y < (cellPosition.y+1) * gridsize; y++ ) {
            for( int x = cellPosition.x * gridsize; x < (cellPosition.x+1) * gridsize; x++ ) {
                if( map.readAt(x, y) == value ) {
                    result.add(new Vector2d<>(x, y));
                }
            }
        }

        return result;
    }

}
