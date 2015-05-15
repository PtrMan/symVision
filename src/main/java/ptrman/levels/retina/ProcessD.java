package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;
import ptrman.levels.retina.helper.SpatialListMap2d;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.*;

import static java.util.Collections.sort;
import static ptrman.math.ArrayRealVectorHelper.*;
import static ptrman.math.Math.getRandomElements;

// TODO< remove detectors which are removable which have a activation less than <constant> * sumofAllActivations >
/**
 * detects lines
 * 
 * forms line hypothesis and tries to strengthen it
 * uses the method of the least squares to fit the potential lines
 * each line detector either will survive or decay if it doesn't receive enought fitting points
 * 
 */
public class ProcessD implements IProcess {

    public void preSetupSet(double maximalDistanceOfPositions) {
        this.maximalDistanceOfPositions = maximalDistanceOfPositions;
    }

    public void set(Queue<ProcessA.Sample> inputSampleQueue) {
        this.inputSampleQueue = inputSampleQueue;
    }

    public List<RetinaPrimitive> getResultRetinaPrimitives() {
        return resultRetinaPrimitives;
    }

    private static class LineDetectorWithMultiplePoints {

        public List<ArrayRealVector> cachedSamplePositions;
        public List<Integer> integratedSampleIndices;

        
        public double m, n;
        
        public double mse = 0.0f;
        
        public boolean isLocked = false; // has the detector received enought activation so it stays?

        public int commonObjectId = -1;
        
        public boolean doesContainSampleIndex(int index)
        {
            return integratedSampleIndices.contains(index);
        }
        
        public double getActivation() {
            return integratedSampleIndices.size() + (Parameters.getProcessdMaxMse() - mse)*Parameters.getProcessdLockingActivationScale();
        }

        public boolean isCommonObjectIdValid() {
            return commonObjectId != -1;
        }
        
        public ArrayRealVector projectPointOntoLine(ArrayRealVector point) {
            if( isYAxisSingularity() ) {
                // call isn't allowed
                throw new RuntimeException("internal error");
                //return projectPointOntoLineForSingular(point);
            }
            else {
                return projectPointOntoLineForNonsingular(point);
            }
        }
        
        /*private Vector2d<Float> projectPointOntoLineForSingular(Vector2d<Float> point)
        {
            return new Vector2d<Float>(point.x, horizontalOffset);
        }*/
        
        public ArrayRealVector projectPointOntoLineForNonsingular(ArrayRealVector point) {
            ArrayRealVector lineDirection = getNormalizedDirection();
            ArrayRealVector diffFromAToPoint = point.subtract(new ArrayRealVector(new double[]{0.0f, n}));
            double dotResult = lineDirection.dotProduct(diffFromAToPoint);

            return new ArrayRealVector(new double[]{0.0f, n}).add(getScaled(lineDirection, dotResult));
        }
        
        private ArrayRealVector getNormalizedDirection() {
            if( isYAxisSingularity() ) {
                throw new RuntimeException("internal error");
            }
            else {
                return ArrayRealVectorHelper.normalize(new ArrayRealVector(new double[]{1.0f, m}));
            }
        }
        
        // TODO< just simply test flag >
        public boolean isYAxisSingularity() {
            return Double.isInfinite(m);
        }
        
        public double getHorizontalOffset(List<ProcessA.Sample> samples) {
            Assert.Assert(isYAxisSingularity(), "");
            
            int sampleIndex = integratedSampleIndices.get(0);
            return samples.get(sampleIndex).position.getDataRef()[0];
        }

        public double getLength() {
            List<ArrayRealVector> sortedSamplePositions = getSortedSamplePositions(this);

            Assert.Assert(sortedSamplePositions.size() >= 2, "samples size must be equal or greater than two");

            // it doesn't care if the line is singuar or not, the distance is always the length
            final ArrayRealVector lastSamplePosition = sortedSamplePositions.get(sortedSamplePositions.size()-1);
            final ArrayRealVector firstSamplePosition = sortedSamplePositions.get(0);

            return lastSamplePosition.subtract(firstSamplePosition).getNorm();
        }
    }

    @Override
    public void setImageSize(final Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {
        Assert.Assert((imageSize.x % gridcellSize) == 0, "");
        Assert.Assert((imageSize.y % gridcellSize) == 0, "");

        accelerationMap = new SpatialListMap2d<>(imageSize, gridcellSize);
    }

    @Override
    public void processData() {
        // take samples from queue and put into array
        List<ProcessA.Sample> samplesAsList = new ArrayList<>();

        final int inputSampleQueueSize = inputSampleQueue.size();

        for( int i = 0; i < inputSampleQueueSize; i++ ) {
            samplesAsList.add(inputSampleQueue.poll());
        }


        resultRetinaPrimitives = detectLines(samplesAsList);
    }
    
    /**
     * 
     * \param samples doesn't need to be filtered for endo/exosceleton points, it does it itself
     * 
     * \return only the surviving line segments
     */
    private List<RetinaPrimitive> detectLines(List<ProcessA.Sample> samples) {
        List<LineDetectorWithMultiplePoints> multiplePointsLineDetector = new ArrayList<>();

        final List<ProcessA.Sample> workingSamples = samples;

        if( workingSamples.isEmpty() ) {
            return new ArrayList<>();
        }

        // we store all samples inside the acceleration datastructure
        int sampleIndex = 0; // NOTE< index in endosceletonPoint / workingSamples >
        for( final ProcessA.Sample iterationSample : workingSamples ) {
            putSampleIndexAtPositionIntoAccelerationDatastructure(arrayRealVectorToInteger(iterationSample.position), sampleIndex);
            sampleIndex++;
        }

        // TODO< add constant >
        int numberOfTries = (int)( samples.size() * 3.5f );

        // TODO< imagesize >
        double maxLength = Math.sqrt(100.0f*100.0f + 80.0f*80.0f);

        final int numberOfMaximalLengthCycles = 5;

        for( int lengthCycle = 0; lengthCycle < numberOfMaximalLengthCycles; lengthCycle++ ) {
            // pick out a random cell and pick out a random sample in it and try to build a (small) line out of it
            for( int tryCounter = 0; tryCounter < numberOfTries; tryCounter++ ) {
                List<Integer> keys = new ArrayList<>(accelerationMapCellUsed.keySet());
                final int randomCellPositionIndex = keys.get(random.nextInt(keys.size()));
                final Vector2d<Integer> randomCellPosition = new Vector2d<>(randomCellPositionIndex % accelerationMap.getWidth(), randomCellPositionIndex / accelerationMap.getWidth());

                // pick out three points at random
                // TODO< better strategy >

                List<Integer> chosenCandidateSampleIndices = new ArrayList<>();

                {
                    final List<Integer> allCandidateSampleIndices = getAllIndicesOfSamplesOfCellAndNeightborCells(randomCellPosition);

                    if( allCandidateSampleIndices.size() < 3 ) {
                        continue;
                    }

                    final List<Integer> allCandidateSampleIndicesChosenIndices = getRandomElements(allCandidateSampleIndices.size(), 3, random);

                    for( final int iterationChosenIndex : allCandidateSampleIndicesChosenIndices ) {
                        final int currentChosenCandidateSampleIndex = allCandidateSampleIndices.get(iterationChosenIndex);
                        chosenCandidateSampleIndices.add(currentChosenCandidateSampleIndex);
                    }
                }







                final List<ProcessA.Sample> selectedSamples = getSamplesByIndices(workingSamples, chosenCandidateSampleIndices);

                final boolean doAllSamplesHaveId = doAllSamplesHaveObjectId(selectedSamples);
                if( !doAllSamplesHaveId ) {
                    continue;
                }

                // check if object ids are the same
                final boolean objectIdsOfSamplesTheSame = areObjectIdsTheSameOfSamples(selectedSamples);
                if( !objectIdsOfSamplesTheSame ) {
                    continue;
                }

                final List<ArrayRealVector> positionsOfSamples = getPositionsOfSamples(selectedSamples);

                final ArrayRealVector averageOfPositionsOfSamples = getAverage(positionsOfSamples);
                final double currentMaximalDistanceOfPositions = getMaximalDistanceOfPositionsTo(positionsOfSamples, averageOfPositionsOfSamples);

                if( currentMaximalDistanceOfPositions > Math.min(maximalDistanceOfPositions, maxLength*0.5f) ) {
                    // one point is too far away from the average position, so this line is not formed
                    continue;
                }
                // else we are here

                final RegressionForLineResult regressionResult = calcRegressionForPoints(positionsOfSamples);

                if( regressionResult.mse > Parameters.getProcessdMaxMse() ) {
                    continue;
                }
                // else we are here


                // create new line detector
                LineDetectorWithMultiplePoints createdLineDetector = new LineDetectorWithMultiplePoints();
                createdLineDetector.integratedSampleIndices = chosenCandidateSampleIndices;
                createdLineDetector.cachedSamplePositions = positionsOfSamples;



                Assert.Assert(areObjectIdsTheSameOfSamples(selectedSamples), "");
                createdLineDetector.commonObjectId = selectedSamples.get(0).objectId;

                Assert.Assert(createdLineDetector.integratedSampleIndices.size() >= 2, "");
                // the regression mse is not defined if it are only two points

                boolean addCreatedLineDetector = false;

                if( createdLineDetector.integratedSampleIndices.size() == 2 ) {
                    createdLineDetector.mse = 0.0f;

                    createdLineDetector.n = regressionResult.n;
                    createdLineDetector.m = regressionResult.m;

                    addCreatedLineDetector = true;
                }
                else {
                    if( regressionResult.mse < Parameters.getProcessdMaxMse() ) {
                        createdLineDetector.mse = regressionResult.mse;

                        createdLineDetector.n = regressionResult.n;
                        createdLineDetector.m = regressionResult.m;

                        addCreatedLineDetector = true;
                    }
                }


                if( createdLineDetector.getLength() > maxLength ) {
                    continue;
                }
                // else we are here

                if( addCreatedLineDetector ) {
                    multiplePointsLineDetector.add(createdLineDetector);
                }
            }


            // compile statistics
            SummaryStatistics lineLengthStatistics = new SummaryStatistics();

            for( LineDetectorWithMultiplePoints currentLineDetectorWithMultipleLines : multiplePointsLineDetector ) {
                double length = currentLineDetectorWithMultipleLines.getLength();
                lineLengthStatistics.addValue(length);
            }



            final double currentLineLengthMean = lineLengthStatistics.getMean();
            final double newMaxLength = currentLineLengthMean * 0.9f; // TODO< constant >
            
            System.out.println("length mean " + Double.toString(currentLineLengthMean));

            maxLength = Math.min(maxLength, newMaxLength);
            final double finalizedMaxLength = maxLength;

            final List<LineDetectorWithMultiplePoints> oldLines = copyLineDetectors(multiplePointsLineDetector);

            // throw out
            multiplePointsLineDetector.removeIf(candidate -> candidate.getLength() > finalizedMaxLength);

            if( multiplePointsLineDetector.size() == 0 ) {
                multiplePointsLineDetector = oldLines;
                break;
            }
        }






        // split the detectors into one or many lines
        List<RetinaPrimitive> resultSingleDetectors = splitDetectorsIntoLines(multiplePointsLineDetector);
        
        return resultSingleDetectors;
    }

    private static List<LineDetectorWithMultiplePoints> copyLineDetectors(final List<LineDetectorWithMultiplePoints> lineDetectors) {
        List<LineDetectorWithMultiplePoints> result = new ArrayList<>();

        for( final LineDetectorWithMultiplePoints iterationLineDetector : lineDetectors ) {
            result.add(iterationLineDetector);
        }

        return result;
    }

    private static boolean doAllSamplesHaveObjectId(final List<ProcessA.Sample> samples) {
        for( final ProcessA.Sample iterationSamples : samples ) {
            if( !iterationSamples.isObjectIdValid() ) {
                return false;
            }
        }

        return true;
    }

    private static boolean areObjectIdsTheSameOfSamples(final List<ProcessA.Sample> samples) {
        Assert.Assert(samples.get(0).isObjectIdValid(), "");

        final int objectId = samples.get(0).objectId;

        for( final ProcessA.Sample iterationSamples : samples ) {
            Assert.Assert(iterationSamples.isObjectIdValid(), "");

            if( iterationSamples.objectId != objectId ) {
                return false;
            }
        }

        return true;
    }

    private static List<ArrayRealVector> getPositionsOfSamples(final List<ProcessA.Sample> samples) {
        List<ArrayRealVector> resultPositions = new ArrayList<>();

        for( final ProcessA.Sample iterationSample : samples ) {
            resultPositions.add(iterationSample.position);
        }

        return resultPositions;
    }

    private static double getMaximalDistanceOfPositionsTo(final List<ArrayRealVector> positions, final ArrayRealVector comparePosition) {
        double maxDistance = 0.0;

        for( final ArrayRealVector iterationPosition : positions) {
            final double currentDistance = iterationPosition.getDistance(comparePosition);
            maxDistance = java.lang.Math.max(maxDistance, currentDistance);
        }

        return maxDistance;
    }

    private List<Vector2d<Integer>> getUnionOfCellsByPositions(final List<ArrayRealVector> positions) {
        Set<Vector2d<Integer>> tempSet = new HashSet<>();

        for( final ArrayRealVector iterationPosition : positions ) {
            final Vector2d<Integer> positionAsInteger = arrayRealVectorToInteger(iterationPosition);
            final Vector2d<Integer> cellPosition = new Vector2d<>(positionAsInteger.x / gridcellSize, positionAsInteger.y / gridcellSize);

            tempSet.add(cellPosition);
        }

        List<Vector2d<Integer>> resultList = new ArrayList<>();
        resultList.addAll(tempSet);
        return resultList;
    }

    private List<Integer> getAllIndicesOfSamplesOfCellAndNeightborCells(final Vector2d<Integer> centerCellPosition) {
        List<Integer> result = new ArrayList<>();

        for( int y = centerCellPosition.y - 1; y < centerCellPosition.y + 1; y++ ) {
            for( int x = centerCellPosition.x - 1; x < centerCellPosition.x + 1; x++ ) {
                if( !accelerationMap.inBounds(new Vector2d<>(x, y)) ) {
                    continue;
                }

                final List<Integer> listAtPosition = accelerationMap.readAt(x, y);

                if( listAtPosition != null ) {
                    result.addAll(listAtPosition);
                }
            }
        }

        return result;
    }

    private static List<ProcessA.Sample> getSamplesByIndices(final List<ProcessA.Sample> samples, final List<Integer> indices) {
        List<ProcessA.Sample> resultPositions = new ArrayList<>();

        for( final int index : indices ) {
            resultPositions.add(samples.get(index));
        }

        return resultPositions;
    }

    private void putSampleIndexAtPositionIntoAccelerationDatastructure(final Vector2d<Integer> position, final int sampleIndex) {
        final Vector2d<Integer> cellPosition = new Vector2d<>(position.x / gridcellSize, position.y /gridcellSize);

        if( accelerationMap.readAt(cellPosition.x, cellPosition.y) == null ) {
            Assert.Assert(!accelerationMapCellUsed.containsKey(cellPosition.x + cellPosition.y * accelerationMap.getWidth()), "");

            accelerationMap.setAt(cellPosition.x, cellPosition.y, new ArrayList<>(Arrays.asList(new Integer[]{sampleIndex})));
            accelerationMapCellUsed.put(cellPosition.x + cellPosition.y * accelerationMap.getWidth(), true);
        }
        else {
            Assert.Assert(accelerationMapCellUsed.containsKey(cellPosition.x + cellPosition.y * accelerationMap.getWidth()), "");

            List<Integer> indices = accelerationMap.readAt(cellPosition.x, cellPosition.y);
            indices.add(sampleIndex);
        }
    }




    /**
     * works by counting the "overlapping" pixel coordinates, chooses the axis with the less overlappings
     *  
     */
    private static RegressionForLineResult calcRegressionForPoints(List<ArrayRealVector> positions) {
        SimpleRegression regression;
        
        int overlappingPixelsOnX, overlappingPixelsOnY;
        
        RegressionForLineResult regressionResultForLine;
        
        overlappingPixelsOnX = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.X);
        overlappingPixelsOnY = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.Y);
        
        regression = new SimpleRegression();
        
        regressionResultForLine = new RegressionForLineResult();
        
        if( overlappingPixelsOnX <= overlappingPixelsOnY ) {
            // regression on x axis
            
            for( ArrayRealVector iterationPosition : positions ) {
                regression.addData(iterationPosition.getDataRef()[0], iterationPosition.getDataRef()[1]);
            }
            
            regressionResultForLine.mse = regression.getMeanSquareError();
            regressionResultForLine.n = regression.getIntercept();
            regressionResultForLine.m = regression.getSlope();
        }
        else {
            // regression on y axis
            // we switch x and y and calculate m and n from the regression result
            
            for( ArrayRealVector iterationPosition : positions ) {
                regression.addData(iterationPosition.getDataRef()[1], iterationPosition.getDataRef()[0]);
            }
            
            // calculate m and n
            double regressionM = regression.getSlope();
            double regressionN = regression.getIntercept();
            
            double m = 1.0f/regressionM;
            ArrayRealVector pointOnRegressionLine = new ArrayRealVector(new double[]{regressionN, 0.0});
            double n = pointOnRegressionLine.getDataRef()[1] - m * pointOnRegressionLine.getDataRef()[0];
            
            regressionResultForLine.mse = regression.getMeanSquareError();
            regressionResultForLine.n = n;
            regressionResultForLine.m = m;
        }
        
        return regressionResultForLine;
    }
    
    private static int calcCountOfOverlappingPixelsForAxis(List<ArrayRealVector> positions, EnumAxis axis) {
        double maxCoordinatOnAxis = getMaximalCoordinateForPoints(positions, axis);
        int arraysizeOfDimension = Math.round((float)maxCoordinatOnAxis)+1;
        int[] dimensionCounter = new int[arraysizeOfDimension];
        
        for( ArrayRealVector iterationPosition : positions ) {
            int dimensionCounterIndex = Math.round((float)Helper.getAxis(iterationPosition, axis));
            
            dimensionCounter[dimensionCounterIndex]++;
        }
        
        // count the "rows" where the count is greater than 1
        int overlappingCounter = 0;

        for( int arrayI = 0; arrayI < dimensionCounter.length; arrayI++ ) {
            if( dimensionCounter[arrayI] > 1 ) {
                overlappingCounter++;
            }
        }
        
        return overlappingCounter;
    }
    
    // used to calculate the arraysize
    private static double getMaximalCoordinateForPoints(List<ArrayRealVector> positions, EnumAxis axis) {
        double max = 0;
        
        for( ArrayRealVector iterationPosition : positions ) {
            max = Math.max(max, Helper.getAxis(iterationPosition, axis));
        }
        
        return max;
    }
    
    private static List<RetinaPrimitive> splitDetectorsIntoLines(List<LineDetectorWithMultiplePoints> lineDetectorsWithMultiplePoints) {
        List<RetinaPrimitive> result;
        
        result = new ArrayList<>();
        
        for( LineDetectorWithMultiplePoints iterationDetector : lineDetectorsWithMultiplePoints ) {
            result.addAll(splitDetectorIntoLines(iterationDetector));
        }
        
        return result;
    }

    private static List<ArrayRealVector> getSortedSamplePositions(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints) {
        List<ArrayRealVector> samplePositions = new ArrayList<>();

        if( lineDetectorWithMultiplePoints.isYAxisSingularity() ) {
            for( ArrayRealVector iterationSamplePosition : lineDetectorWithMultiplePoints.cachedSamplePositions ) {
                samplePositions.add(iterationSamplePosition);
            }

            sort(samplePositions, new VectorComperatorByAxis(EnumAxis.Y));
        }
        else {
            // project
            for( ArrayRealVector iterationSamplePosition : lineDetectorWithMultiplePoints.cachedSamplePositions ) {
                ArrayRealVector projectedSamplePosition = lineDetectorWithMultiplePoints.projectPointOntoLine(iterationSamplePosition);

                samplePositions.add(projectedSamplePosition);
            }

            sort(samplePositions, new VectorComperatorByAxis(EnumAxis.X));
        }

        return samplePositions;
    }
    
    private static List<RetinaPrimitive> splitDetectorIntoLines(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints) {
        List<ArrayRealVector> sortedSamplePositions = getSortedSamplePositions(lineDetectorWithMultiplePoints);

        if( lineDetectorWithMultiplePoints.isYAxisSingularity() ) {
            return clusterPointsFromLinedetectorToLinedetectors(lineDetectorWithMultiplePoints.commonObjectId, sortedSamplePositions, EnumAxis.Y);
        }
        else {
            return clusterPointsFromLinedetectorToLinedetectors(lineDetectorWithMultiplePoints.commonObjectId, sortedSamplePositions, EnumAxis.X);
        }
    }
    
    
    private static List<RetinaPrimitive> clusterPointsFromLinedetectorToLinedetectors(final int objectId, final List<ArrayRealVector> pointPositions, final EnumAxis axis) {
        List<RetinaPrimitive> resultSingleLineDetectors = new ArrayList<>();

        boolean nextIsNewLineStart = true;

        ArrayRealVector lineStartPosition = pointPositions.get(0);
        double lastAxisPosition = Helper.getAxis(pointPositions.get(0), axis);

        for( ArrayRealVector iterationPoint : pointPositions ) {
            if( nextIsNewLineStart ) {
                lineStartPosition = iterationPoint;
                lastAxisPosition = Helper.getAxis(iterationPoint, axis);

                nextIsNewLineStart = false;

                continue;
            }
            // else we are here

            if( Helper.getAxis(iterationPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE ) {
                lastAxisPosition = Helper.getAxis(iterationPoint, axis);
            }
            else {
                // form a new line
                RetinaPrimitive newPrimitive = RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, iterationPoint));
                newPrimitive.objectId = objectId;
                resultSingleLineDetectors.add(newPrimitive);

                nextIsNewLineStart = true;
            }
        }

        // form a new line for the last point
        ArrayRealVector lastPoint = pointPositions.get(pointPositions.size()-1);

        if( !nextIsNewLineStart && Helper.getAxis(lastPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE ) {
            RetinaPrimitive newPrimitive = RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, lastPoint));
            newPrimitive.objectId = objectId;
            resultSingleLineDetectors.add(newPrimitive);
        }
        
        return resultSingleLineDetectors;
    }
    
    private static List<ProcessA.Sample> filterEndosceletonPoints(List<ProcessA.Sample> samples) {
        List<ProcessA.Sample> filtered;
        
        filtered = new ArrayList<>();
        
        for( ProcessA.Sample iterationSample : samples ) {
            if( iterationSample.type == ProcessA.Sample.EnumType.ENDOSCELETON ) {
                filtered.add(iterationSample);
            }
        }
        
        return filtered;
    }


    // TODO< belongs into dedicated helper >
    static private class Helper {
        private static boolean isDistanceBetweenPositionsBelow(ArrayRealVector a, ArrayRealVector b, double maxDistance) {
            return a.subtract(b).getNorm() < maxDistance;
        }

        private static double getAxis(ArrayRealVector vector, EnumAxis axis) {
            if( axis == EnumAxis.X ) {
                return vector.getDataRef()[0];
            }
            else {
                return vector.getDataRef()[1];
            }
        }
    }


    private static class VectorComperatorByAxis implements Comparator<ArrayRealVector> {
        
        public VectorComperatorByAxis(EnumAxis axis)
        {
            this.axis = axis;
        }
        
        @Override
        public int compare(ArrayRealVector a, ArrayRealVector b) {
            if( Helper.getAxis(a, axis) > Helper.getAxis(b, axis) ) {
                return 1;
            }
            
            return -1;
        }
        
        private final EnumAxis axis;
    }
    
    private static class RegressionForLineResult {
        public double mse;
        
        public double m, n;
    }
    
    private enum EnumAxis {
        X,
        Y
    }

    private Vector2d<Integer> imageSize;

    private int gridcellSize = 8;

    private Random random = new Random();

    // each cell contains the incides of the points/samples inside the accelerationMap
    private SpatialListMap2d<Integer> accelerationMap;

    private Map<Integer, Boolean> accelerationMapCellUsed = new HashMap<>();

    private List<RetinaPrimitive> resultRetinaPrimitives;

    private double maximalDistanceOfPositions;

    private Queue<ProcessA.Sample> inputSampleQueue;
}
