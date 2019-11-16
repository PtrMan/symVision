package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.*;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;


/**
 *
 * java version of ProcessB
 */
public class ProcessB extends AbstractProcessB {


    @Override
    public void set(IMap2d<Boolean> map, ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<ProcessA.Sample> outputSampleConnector) {
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
     * we use the whole image, in phaeaco he worked with the incomplete image with the guiding of processA, this is not implemented that way
     */
    @Override
    public void processData() {
        List<ProcessA.Sample> samples = inputSampleConnector.getWorkspace();

        Vector2d<Integer> foundPosition;
        
        final int MAXRADIUS = (int)Math.ceil( Math.sqrt( imageSize.x*imageSize.x + imageSize.y*imageSize.y) ); // (int)Math.sqrt(squaredDistance(new double[]{(double)imageSize.x, (double)imageSize.y}));

        final int GRIDSIZE_FOR_SPATIALACCELERATEDMAP2D = 8;

        counterCellPositiveCandidates = 0;
        counterCellCandidates = 0;

        this.map = convertMapToFastBooleanMap2d(inputMap);

        spatialAcceleratedMap2d = new SpatialAcceleratedMap2d(map, GRIDSIZE_FOR_SPATIALACCELERATEDMAP2D);
        spatialAcceleratedMap2d.recalculateGridCellStateMap();
        
        for( ProcessA.Sample iterationSample : samples ) {
            Tuple2<Vector2d<Integer>, Double> nearestResult;
            
            nearestResult = findNearestPositionWhereMapIs(false, arrayRealVectorToInteger(iterationSample.position, ArrayRealVectorHelper.EnumRoundMode.DOWN), map, MAXRADIUS);
            if( nearestResult == null ) {
                iterationSample.altitude = ((MAXRADIUS+1)*2)*((MAXRADIUS+1)*2);
                continue;
            }
            // else here

            // create a new sample to the output connector
            ProcessA.Sample outputSample = iterationSample.getClone();
            outputSample.altitude =  nearestResult.e1;

            outputSampleConnector.add(outputSample);
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
    private Tuple2<Vector2d<Integer>, Double> findNearestPositionWhereMapIs(boolean value, Vector2d<Integer> position, IMap2d<Boolean> image, int radius) {
        /* debug
        if( position.x > 80 && position.x < 90 && position.y > 60 && position.y < 70 ) {
            int x = 0;
        }
        */

        Map2d<Boolean> debugMap = new Map2d<>(spatialAcceleratedMap2d.getSize().x, spatialAcceleratedMap2d.getSize().y);
        for( int y = 0; y < debugMap.getLength(); y++ ) {
            for( int x = 0; x < debugMap.getWidth(); x++ ) {
                debugMap.setAt(x, y, false);
            }
        }

        final ArrayRealVector positionReal = ptrman.math.ArrayRealVectorHelper.integerToArrayRealVector(position);

        final Vector2d<Integer> gridCenterPosition = spatialAcceleratedMap2d.getGridPositionOfPosition(position);

        final int gridMaxSearchRadius = 2 + radius / spatialAcceleratedMap2d.getGridsize();

        // set this to int.max when the radius is not limited
        int radiusToScan = gridMaxSearchRadius;

        Vector2d<Integer> nearestPixelCandidate = null;
        double nearestPixelCandidateDistanceSquared = Double.MAX_VALUE;

        for( int currentGridRadius = 0; currentGridRadius < radiusToScan; currentGridRadius++ ) {
            final List<Vector2d<Integer>> gridCellsToScan;

            // if we are at the center we need to scan only the center
            if( currentGridRadius == 0 ) {
                gridCellsToScan = new ArrayList<>();
                gridCellsToScan.add(gridCenterPosition);
            }
            else {
                gridCellsToScan = spatialAcceleratedMap2d.getGridLocationsWithNegativeDirectionOfGridRadius(gridCenterPosition, currentGridRadius);
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
            gridCellsToScan.removeIf(cellPosition -> !spatialAcceleratedMap2d.canValueBeFoundInCell(cellPosition, value));

            // statistics
            counterCellCandidates += gridCellsToScan.size();
            counterCellPositiveCandidates += gridCellsToScan.size();

            final List<Vector2d<Integer>> pixelPositionsToCheck = getPositionsOfCandidatePixelsOfCells(gridCellsToScan, value);

            // pixel scan logic

            if( !pixelPositionsToCheck.isEmpty() ) {
                // do this because we need to scan the next radius too
                radiusToScan = java.lang.Math.min(radiusToScan, currentGridRadius+1+1);
            }

            for( final Vector2d<Integer> iterationPixelPosition : pixelPositionsToCheck ) {
                final ArrayRealVector iterationPixelPositionReal = ptrman.math.ArrayRealVectorHelper.integerToArrayRealVector(iterationPixelPosition);

                final ArrayRealVector diff = positionReal.subtract(iterationPixelPositionReal);
                final double currentDistanceSquared = diff.dotProduct(diff);
                if( currentDistanceSquared < nearestPixelCandidateDistanceSquared ) {
                    nearestPixelCandidateDistanceSquared = currentDistanceSquared;
                    nearestPixelCandidate = iterationPixelPosition;
                }
            }
        }

        if( nearestPixelCandidate == null ) {
            return null;
        }
        else {
            final double nearestPixelCandidateDistance = java.lang.Math.sqrt(nearestPixelCandidateDistanceSquared);
            return new Tuple2<>(nearestPixelCandidate, nearestPixelCandidateDistance);
        }
    }

    private List<Vector2d<Integer>> getPositionsOfCandidatePixelsOfCells(final List<Vector2d<Integer>> cellPositions, final boolean value) {
        List<Vector2d<Integer>> result = new ArrayList<>();

        Assert.Assert(!value, "only implemented for value = false");

        for( final Vector2d<Integer> iterationCellPosition : cellPositions ) {
            result.addAll(getPositionsOfCandidatePixelsOfCellWhereFalse(iterationCellPosition));
        }

        return result;
    }

    private List<Vector2d<Integer>> getPositionsOfCandidatePixelsOfCellWhereFalse(final Vector2d<Integer> cellPosition) {
        List<Vector2d<Integer>> result = new ArrayList<>();

        final int gridsize = spatialAcceleratedMap2d.getGridsize();

        Assert.Assert(gridsize == 8, "only implemented for gridsize = 8");

        final boolean value = false; // value to search for, for easier to change code

        for( int y = cellPosition.y * gridsize; y < (cellPosition.y+1) * gridsize; y++ ) {
            final int byteValueAt = map.readByteAtInt(cellPosition.x * gridsize, y);

            if( byteValueAt != 0xff ) {
                for( int x = cellPosition.x * gridsize; x < (cellPosition.x+1) * gridsize; x++ ) {
                    if(!map.readAt(x, y)) {
                        result.add(new Vector2d<>(x, y));
                    }
                }
            }
        }

        return result;
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

    private SpatialAcceleratedMap2d spatialAcceleratedMap2d;
    private FastBooleanMap2d map;

    private int counterCellPositiveCandidates;
    private int counterCellCandidates;
    private IMap2d<Boolean> inputMap;
    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<ProcessA.Sample> outputSampleConnector;
}
