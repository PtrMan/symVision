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

import com.google.common.collect.Lists;
//import com.gs.collections.api.list.primitive.IntList;
//import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
//import com.gs.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.eclipse.collections.api.block.procedure.primitive.IntBooleanProcedure;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.levels.retina.helper.SpatialDrawer;
import ptrman.levels.retina.helper.SpatialListMap2d;
import ptrman.misc.Assert;

import java.util.*;

import static ptrman.Datastructures.Vector2d.IntegerHelper.add;
import static ptrman.Datastructures.Vector2d.IntegerHelper.sub;
import static ptrman.math.ArrayRealVectorHelper.*;
import static ptrman.math.Maths.getRandomElements;
import static ptrman.math.Maths.squaredDistance;

/**
 * proposal of lines from points
 * <p>
 * forms line hypothesis and tries to strengthen it
 * uses the method of the least squares to fit the potential lines
 * each line detector either will survive or decay if it doesn't receive enought fitting points
 *
 * is using a kind of simulated annealing to weed out "old" not useful hypothesis of lines
 */
public class ProcessDAnnealing implements IProcess {
    private static class LineDetectors {
        public LineDetectors(List<LineDetectorWithMultiplePoints> lineDetectors) {
            this.lineDetectors = lineDetectors;
        }

        public List<LineDetectorWithMultiplePoints> lineDetectors;
    }

    public void set(ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<RetinaPrimitive> outputLineDetectorConnector) {
        this.inputSampleConnector = inputSampleConnector;
        this.outputLineDetectorConnector = outputLineDetectorConnector;
    }


    @Override
    public void setImageSize(final Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {
    }

    @Override
    public void preProcessData() {
        Assert.Assert((imageSize.x % gridcellSize) == 0, "");
        Assert.Assert((imageSize.y % gridcellSize) == 0, "");

        // small size hack because else the map is accessed out of range
        accelerationMap = new SpatialListMap2d<>(new Vector2d<>(imageSize.x + gridcellSize, imageSize.y + gridcellSize * 2), gridcellSize);

        accelerationMapCellUsed = new IntBooleanHashMap();
    }

    Mean lineLengthStatistics = new Mean();

    @Override
    public void processData() {
        processData(1f);
    }

    // candidates which are annealed
    public List<LineDetectorWithMultiplePoints> anealedCandidates = new ArrayList<>();


    // 5 for testing
    public int anealedCandidatesMaxCount = 5; // maximal number of line segments which are considered

    // sorts anealedCandidates by activation and throws items with to low activation away
    public void sortByActivationAndThrowAway() {
        if (anealedCandidates.size() > 0) {
            int here = 5;
        }

        anealedCandidates.sort((a, b) -> a.getActivation() < b.getActivation() ? 1 : -1);

        // limit size
        while(anealedCandidates.size() > anealedCandidatesMaxCount) {
            anealedCandidates.remove(anealedCandidatesMaxCount-1);
        }
    }

    // tries to sample a new line candidate
    public void sampleNew() {
        List<LineDetectorWithMultiplePoints> multiplePointsLineDetector = new ArrayList<>();

        final List<ProcessA.Sample> workingSamples = inputSampleConnector.getWorkspace();

        if (workingSamples.isEmpty()) {
            return;
        }


        // we store all samples inside the acceleration datastructure
        int sampleIndex = 0; // NOTE< index in endosceletonPoint / workingSamples >
        for (final ProcessA.Sample iterationSample : workingSamples) {
            putSampleIndexAtPositionIntoAccelerationDatastructure(arrayRealVectorToInteger(iterationSample.position, EnumRoundMode.DOWN), sampleIndex);
            sampleIndex++;
        }

        float throttle = 1.0f;
        int numberOfTries = (int) (workingSamples.size() * HardParameters.ProcessD.SAMPLES_NUMBER_OF_TRIES_MULTIPLIER * throttle);

        final double maxLength = java.lang.Math.sqrt(squaredDistance(new double[]{imageSize.x, imageSize.y}));

        final int numberOfMaximalLengthCycles = 5;

        Deque<LineDetectors> lineDetectorsRecords = new ArrayDeque<>();
        float selectionProbability = ((float)numberOfTries) / accelerationMapCellUsed.size();

        IntBooleanProcedure eachCell = (k, v) -> {

            if (random.nextFloat() > selectionProbability) return;
            final int randomCellPositionIndex = k; //getRandomElementFromSet(accelerationMapCellUsed, random);


            final Vector2d<Integer> randomCellPosition = new Vector2d<>(
                randomCellPositionIndex % accelerationMap.getWidth(),
                randomCellPositionIndex / accelerationMap.getWidth());


            final IntArrayList allCandidateSampleIndices;

            // strategy for getting a "protoline"

            final int ACCELERATIONSTRUCUTRE_LINE_CANDIDATES_RADIUS = 1;

            // variables for LineDetectorWithMultiplePoints
            final ArrayRealVector spatialAccelerationLineDirection;
            final Vector2d<Integer> spatialAccelerationCenterPosition = randomCellPosition;

            // pick out possible direction(s) from the neightbor (acceleration structure) cells
            final List<Vector2d<Integer>> possibleNeightborAccelerationCellsWithSamples = getAccelerationStructureNeightborCellsWithContent(spatialAccelerationCenterPosition, ACCELERATIONSTRUCUTRE_LINE_CANDIDATES_RADIUS);

            if (possibleNeightborAccelerationCellsWithSamples.isEmpty()) {
                spatialAccelerationLineDirection = null;

                // TODO< get sample indices just from the one cell >
                allCandidateSampleIndices = getAllIndicesOfSamplesOfCellAndNeightborCells(spatialAccelerationCenterPosition);

                if (allCandidateSampleIndices.size() < 3) {
                    return;
                }
            } else {
                // * pick out a random neightbor cell and pick out a random direction for the accelerationstructure line

                final int possibleNeightborAccelerationCellIndex = random.nextInt(possibleNeightborAccelerationCellsWithSamples.size());
                final Vector2d<Integer> chosenAccelerationStructureNeightborCellPosition = possibleNeightborAccelerationCellsWithSamples.get(possibleNeightborAccelerationCellIndex);

                final Vector2d<Integer> cellDelta = sub(chosenAccelerationStructureNeightborCellPosition, spatialAccelerationCenterPosition);
                final Vector2d<Integer> minusCellDelta = new Vector2d<>(-cellDelta.x, -cellDelta.y);
                final Vector2d<Integer> otherCellPosition = add(spatialAccelerationCenterPosition, minusCellDelta);

                final ArrayRealVector absoluteRandomPositionInChosenNeightborCell = calcAbsolutePositionInAccelerationCell(chosenAccelerationStructureNeightborCellPosition);
                final ArrayRealVector absoluteCenterPosition = getAbsolutePositionOfLeftTopCornerOfAccelerationCell(spatialAccelerationCenterPosition).add(new ArrayRealVector(new double[]{0.5 * (double) gridcellSize, 0.5 * (double) gridcellSize}));

                final ArrayRealVector absoluteDiff = absoluteRandomPositionInChosenNeightborCell.subtract(absoluteCenterPosition);
                spatialAccelerationLineDirection = normalize(absoluteDiff);

                // * draw line in acceleration structure

                final List<Vector2d<Integer>> accelerationCandidateCells = SpatialDrawer.getPositionsOfCellsOfLineUnbound(otherCellPosition, chosenAccelerationStructureNeightborCellPosition);

                // * filter out valid candidates
                accelerationCandidateCells.removeIf(Objects::isNull);

                // * select random points from the validAccelerationCandidateCells (happens outside)
                // * fitting (happens outside)

                // TODO< other strategy, select two samples and find all points whcih are not too far away from the line >

                allCandidateSampleIndices = getAllSampleIndicesOfCells(accelerationCandidateCells);
            }


            if (allCandidateSampleIndices.size() < 3) {
                return;
            }

            IntList chosenCandidateSampleIndices = getRandomElements(allCandidateSampleIndices, 3, random);


            final List<ProcessA.Sample> selectedSamples = getSamplesByIndices(workingSamples, chosenCandidateSampleIndices);

            final boolean doAllSamplesHaveId = doAllSamplesHaveObjectId(selectedSamples);
            if (!doAllSamplesHaveId) {
                return;
            }

            // check if object ids are the same
            final boolean objectIdsOfSamplesTheSame = areObjectIdsTheSameOfSamples(selectedSamples);
            if (!objectIdsOfSamplesTheSame) {
                return;
            }

            final List<ArrayRealVector> positionsOfSamples = getPositionsOfSamples(selectedSamples);

            final ArrayRealVector averageOfPositionsOfSamples = getAverage(positionsOfSamples);
            final double currentMaximalDistanceOfPositions = getMaximalDistanceOfPositionsTo(positionsOfSamples, averageOfPositionsOfSamples);

            if (currentMaximalDistanceOfPositions > Math.min(maximalDistanceOfPositions, maxLength * 0.5f)) {
                // one point is too far away from the average position, so this line is not formed
                return;
            }
            // else we are here

            final RegressionForLineResult regressionResult = calcRegressionForPoints(positionsOfSamples);

            if (regressionResult.mse > Parameters.getProcessdMaxMse()) {
                return;
            }
            // else we are here


            // create new line detector
            LineDetectorWithMultiplePoints createdLineDetector = new LineDetectorWithMultiplePoints();
            createdLineDetector.integratedSampleIndices = chosenCandidateSampleIndices;
            createdLineDetector.samples = selectedSamples;

            createdLineDetector.spatialAccelerationLineDirection = spatialAccelerationLineDirection;
            createdLineDetector.spatialAccelerationCenterPosition = spatialAccelerationCenterPosition;


            Assert.Assert(areObjectIdsTheSameOfSamples(selectedSamples), "");
            createdLineDetector.commonObjectId = selectedSamples.get(0).objectId;

            Assert.Assert(createdLineDetector.integratedSampleIndices.size() >= 2, "");
            // the regression mse is not defined if it are only two points

            boolean addCreatedLineDetector = false;

            if (createdLineDetector.integratedSampleIndices.size() == 2) {
                createdLineDetector.mse = 0.0f;

                createdLineDetector.n = regressionResult.n;
                createdLineDetector.m = regressionResult.m;

                addCreatedLineDetector = true;
            } else {
                if (regressionResult.mse < Parameters.getProcessdMaxMse()) {
                    createdLineDetector.mse = regressionResult.mse;

                    createdLineDetector.n = regressionResult.n;
                    createdLineDetector.m = regressionResult.m;

                    addCreatedLineDetector = true;
                }
            }


            if (createdLineDetector.getLength() > maxLength) {
                return;
            }
            // else we are here

            if (addCreatedLineDetector) {
                multiplePointsLineDetector.add(createdLineDetector);
            }
        };

        for (int lengthCycle = 0; lengthCycle < numberOfMaximalLengthCycles; lengthCycle++) {
            // pick out a random cell and pick out a random sample in it and try to build a (small) line out of it


            accelerationMapCellUsed.forEachKeyValue(eachCell);


            // compile statistics
            lineLengthStatistics.clear();

            for (LineDetectorWithMultiplePoints currentLineDetectorWithMultipleLines : multiplePointsLineDetector) {
                double length = currentLineDetectorWithMultipleLines.getLength();
                lineLengthStatistics.increment(length);
            }

            final double currentLineLengthMean = lineLengthStatistics.getResult();

            final double newMaxLength = currentLineLengthMean * HardParameters.ProcessD.LENGTH_MEAN_MULTIPLIER;

            System.out.println("length mean " + currentLineLengthMean);

            final double finalizedMaxLength = Math.min(maxLength, newMaxLength);

            lineDetectorsRecords.push(new LineDetectors(copyLineDetectors(multiplePointsLineDetector)));

            if (currentLineLengthMean < HardParameters.ProcessD.MINIMAL_LINESEGMENTLENGTH) {
                break;
            }

            // throw out
            multiplePointsLineDetector.removeIf(candidate -> candidate.getLength() > finalizedMaxLength);

            if (multiplePointsLineDetector.isEmpty()) {
                break;
            }
        }


        multiplePointsLineDetector.clear();

        // add last n records
        for (int lastNRecordsI = 0; lastNRecordsI < HardParameters.ProcessD.LAST_RECORDS_FROM_LINECANDIDATES_STACK; lastNRecordsI++) {
            if (lineDetectorsRecords.isEmpty()) {
                break;
            }

            anealedCandidates.addAll(lineDetectorsRecords.pop().lineDetectors);
        }

    }

    public void processData(float throttle) {

        for(int i = 0; i < 5; i++) {
            sampleNew();
            sortByActivationAndThrowAway();
        }

        /* TODO 16.11.2019 activate this code again because it is necessary as the last step
        // split the detectors into one or many lines
        final List<RetinaPrimitive> resultSingleDetectors = splitDetectorsIntoLines(multiplePointsLineDetector);

        for (final RetinaPrimitive iterationPrimitive : resultSingleDetectors) {
            outputLineDetectorConnector.add(iterationPrimitive);
        }

         */

    }

    @Override
    public void postProcessData() {

    }

    private IntArrayList getAllSampleIndicesOfCells(final List<Vector2d<Integer>> cellPositions) {
        IntArrayList resultIndices = new IntArrayList();

        for (final Vector2d<Integer> iterationCellPosition : cellPositions) {
            final List<Integer> cellContent = accelerationMap.readAt(iterationCellPosition.xInt(), iterationCellPosition.yInt());

            if (cellContent != null) {
                cellContent.forEach((Integer i) -> {
                    if (i != null) resultIndices.add(i);
                });
            }
        }

        return resultIndices;
    }

    private boolean isAccelerationCellEmpty(final Vector2d<Integer> position) {
        return accelerationMap.readAt(position.x, position.y).isEmpty();
    }

    private ArrayRealVector calcAbsolutePositionInAccelerationCell(final Vector2d<Integer> cellPosition) {
        final ArrayRealVector absoluteTopLeftPositionOfCell = getAbsolutePositionOfLeftTopCornerOfAccelerationCell(cellPosition);
        final ArrayRealVector randomOffset = new ArrayRealVector(new double[]{random.nextDouble() * (double) gridcellSize, random.nextDouble() * (double) gridcellSize});

        return absoluteTopLeftPositionOfCell.add(randomOffset);
    }

    private ArrayRealVector getAbsolutePositionOfLeftTopCornerOfAccelerationCell(final Vector2d<Integer> cellPosition) {
        return new ArrayRealVector(new double[]{(double) gridcellSize * cellPosition.x, (double) gridcellSize * cellPosition.y});
    }


    private List<Vector2d<Integer>> getAccelerationStructureNeightborCellsWithContent(final Vector2d<Integer> centerCellPosition, final int cellRadius) {
        final int minx = java.lang.Math.max(0, centerCellPosition.x - cellRadius);
        final int maxx = java.lang.Math.min(accelerationMap.getWidth(), centerCellPosition.x + cellRadius);

        final int miny = java.lang.Math.max(0, centerCellPosition.y - cellRadius);
        final int maxy = java.lang.Math.min(accelerationMap.getLength(), centerCellPosition.y + cellRadius);

        List<Vector2d<Integer>> resultCellPositions = new ArrayList<>();

        for (int y = miny; y < maxy; y++) {
            for (int x = minx; x < maxx; x++) {
                if (centerCellPosition.x != x && centerCellPosition.y != y && accelerationMap.readAt(x, y) != null) {
                    resultCellPositions.add(new Vector2d<>(x, y));
                }
            }
        }

        return resultCellPositions;
    }

    private static List<LineDetectorWithMultiplePoints> copyLineDetectors(final List<LineDetectorWithMultiplePoints> lineDetectors) {

        List<LineDetectorWithMultiplePoints> result = new ArrayList<>(lineDetectors);

        return result;
    }

    private static boolean doAllSamplesHaveObjectId(final List<ProcessA.Sample> samples) {
        for (final ProcessA.Sample iterationSamples : samples) {
            if (!iterationSamples.isObjectIdValid()) {
                return false;
            }
        }

        return true;
    }

    private static boolean areObjectIdsTheSameOfSamples(final List<ProcessA.Sample> samples) {
        Assert.Assert(samples.get(0).isObjectIdValid(), "");

        final int objectId = samples.get(0).objectId;

        for (final ProcessA.Sample iterationSamples : samples) {
            Assert.Assert(iterationSamples.isObjectIdValid(), "");

            if (iterationSamples.objectId != objectId) {
                return false;
            }
        }

        return true;
    }

    private static List<ArrayRealVector> getPositionsOfSamples(final List<ProcessA.Sample> samples) {
        List<ArrayRealVector> resultPositions = new ArrayList<>();

        for (final ProcessA.Sample iterationSample : samples) {
            resultPositions.add(iterationSample.position);
        }

        return resultPositions;
    }

    private static double getMaximalDistanceOfPositionsTo(final List<ArrayRealVector> positions, final ArrayRealVector comparePosition) {
        double maxDistance = 0.0;

        for (final ArrayRealVector iterationPosition : positions) {
            final double currentDistance = iterationPosition.getDistance(comparePosition);
            maxDistance = java.lang.Math.max(maxDistance, currentDistance);
        }

        return maxDistance;
    }

    private List<Vector2d<Integer>> getUnionOfCellsByPositions(final List<ArrayRealVector> positions) {
        Set<Vector2d<Integer>> tempSet = new HashSet<>();

        for (final ArrayRealVector iterationPosition : positions) {
            final Vector2d<Integer> positionAsInteger = arrayRealVectorToInteger(iterationPosition, EnumRoundMode.DOWN);
            final Vector2d<Integer> cellPosition = new Vector2d<>(positionAsInteger.x / gridcellSize, positionAsInteger.y / gridcellSize);

            tempSet.add(cellPosition);
        }

        List<Vector2d<Integer>> resultList = new ArrayList<>(tempSet);
        return resultList;
    }

    private IntArrayList getAllIndicesOfSamplesOfCellAndNeightborCells(final Vector2d<Integer> centerCellPosition) {
        IntArrayList result = new IntArrayList();

        for (int y = centerCellPosition.y - 1; y < centerCellPosition.y + 1; y++) {
            for (int x = centerCellPosition.x - 1; x < centerCellPosition.x + 1; x++) {
                if (!accelerationMap.inBounds(x, y)) {
                    continue;
                }

                final List<Integer> listAtPosition = accelerationMap.readAt(x, y);

                if (listAtPosition != null) {
                    listAtPosition.forEach( i -> {
                        if (i!=null)
                            result.add(i);
                    });
                }
            }
        }

        return result;
    }

    private static List<ProcessA.Sample> getSamplesByIndices(final List<ProcessA.Sample> samples, final IntList indices) {
        List<ProcessA.Sample> resultPositions = new ArrayList<>(indices.size());
        indices.forEach(index -> resultPositions.add(samples.get(index)));
        return resultPositions;
    }

    private void putSampleIndexAtPositionIntoAccelerationDatastructure(final Vector2d<Integer> position, final int sampleIndex) {
        final Vector2d<Integer> cellPosition = new Vector2d<>(position.x / gridcellSize, position.y / gridcellSize);

        if (accelerationMap == null) {
            throw new RuntimeException("NPE");
        }

        if (accelerationMap.readAt(cellPosition.x, cellPosition.y) == null) {
            Assert.Assert(!accelerationMapCellUsed.containsKey(cellPosition.x + cellPosition.y * accelerationMap.getWidth()), "");

            accelerationMap.setAt(cellPosition.x, cellPosition.y, Lists.newArrayList(sampleIndex));
            accelerationMapCellUsed.put(cellPosition.x + cellPosition.y * accelerationMap.getWidth(), true);
        } else {
            Assert.Assert(accelerationMapCellUsed.containsKey(cellPosition.x + cellPosition.y * accelerationMap.getWidth()), "");

            List<Integer> indices = accelerationMap.readAt(cellPosition.x, cellPosition.y);
            indices.add(sampleIndex);
        }
    }


    /**
     * works by counting the "overlapping" pixel coordinates, chooses the axis with the less overlappings
     */
    private static RegressionForLineResult calcRegressionForPoints(List<ArrayRealVector> positions) {
        SimpleRegression regression;

        int overlappingPixelsOnX, overlappingPixelsOnY;

        RegressionForLineResult regressionResultForLine;

        overlappingPixelsOnX = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.X);
        overlappingPixelsOnY = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.Y);

        regression = new SimpleRegression();

        regressionResultForLine = new RegressionForLineResult();

        if (overlappingPixelsOnX <= overlappingPixelsOnY) {
            // regression on x axis

            for (ArrayRealVector iterationPosition : positions) {
                regression.addData(iterationPosition.getDataRef()[0], iterationPosition.getDataRef()[1]);
            }

            regressionResultForLine.mse = regression.getMeanSquareError();
            regressionResultForLine.n = regression.getIntercept();
            regressionResultForLine.m = regression.getSlope();
        } else {
            // regression on y axis
            // we switch x and y and calculate m and n from the regression result

            for (ArrayRealVector iterationPosition : positions) {
                regression.addData(iterationPosition.getDataRef()[1], iterationPosition.getDataRef()[0]);
            }

            // calculate m and n
            double regressionM = regression.getSlope();
            double regressionN = regression.getIntercept();

            double m = 1.0f / regressionM;
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
        int arraysizeOfDimension = Math.round((float) maxCoordinatOnAxis) + 1;
        int[] dimensionCounter = new int[arraysizeOfDimension];

        for (ArrayRealVector iterationPosition : positions) {
            int dimensionCounterIndex = Math.round((float) Helper.getAxis(iterationPosition, axis));

            dimensionCounter[dimensionCounterIndex]++;
        }

        // count the "rows" where the count is greater than 1
        int overlappingCounter = 0;

        for (int arrayI = 0; arrayI < dimensionCounter.length; arrayI++) {
            if (dimensionCounter[arrayI] > 1) {
                overlappingCounter++;
            }
        }

        return overlappingCounter;
    }

    // used to calculate the arraysize
    private static double getMaximalCoordinateForPoints(List<ArrayRealVector> positions, EnumAxis axis) {
        double max = 0;

        for (ArrayRealVector iterationPosition : positions) {
            max = Math.max(max, Helper.getAxis(iterationPosition, axis));
        }

        return max;
    }

    private static List<RetinaPrimitive> splitDetectorsIntoLines(List<LineDetectorWithMultiplePoints> lineDetectorsWithMultiplePoints) {
        List<RetinaPrimitive> result;

        result = new ArrayList<>();

        for (LineDetectorWithMultiplePoints iterationDetector : lineDetectorsWithMultiplePoints) {
            result.addAll(splitDetectorIntoLines(iterationDetector));
        }

        return result;
    }

    public static List<ArrayRealVector> getSortedSamplePositions(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints) {
        List<ArrayRealVector> samplePositions = new ArrayList<>();

        if (lineDetectorWithMultiplePoints.isYAxisSingularity()) {
            samplePositions.addAll(getPositionsOfSamples(lineDetectorWithMultiplePoints.samples));

            samplePositions.sort(new VectorComperatorByAxis(EnumAxis.Y));
        } else {
            // project
            for (ArrayRealVector iterationSamplePosition : getPositionsOfSamples(lineDetectorWithMultiplePoints.samples)) {
                ArrayRealVector projectedSamplePosition = lineDetectorWithMultiplePoints.projectPointOntoLine(iterationSamplePosition);

                samplePositions.add(projectedSamplePosition);
            }

            samplePositions.sort(new VectorComperatorByAxis(EnumAxis.X));
        }

        return samplePositions;
    }

    public static List<RetinaPrimitive> splitDetectorIntoLines(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints) {
        List<ArrayRealVector> sortedSamplePositions = getSortedSamplePositions(lineDetectorWithMultiplePoints);

        if (lineDetectorWithMultiplePoints.isYAxisSingularity()) {
            return clusterPointsFromLinedetectorToLinedetectors(lineDetectorWithMultiplePoints.commonObjectId, sortedSamplePositions, EnumAxis.Y);
        } else {
            return clusterPointsFromLinedetectorToLinedetectors(lineDetectorWithMultiplePoints.commonObjectId, sortedSamplePositions, EnumAxis.X);
        }
    }


    private static List<RetinaPrimitive> clusterPointsFromLinedetectorToLinedetectors(final int objectId, final List<ArrayRealVector> pointPositions, final EnumAxis axis) {
        List<RetinaPrimitive> resultSingleLineDetectors = new ArrayList<>();

        boolean nextIsNewLineStart = true;

        ArrayRealVector lineStartPosition = pointPositions.get(0);
        double lastAxisPosition = Helper.getAxis(pointPositions.get(0), axis);

        for (ArrayRealVector iterationPoint : pointPositions) {
            if (nextIsNewLineStart) {
                lineStartPosition = iterationPoint;
                lastAxisPosition = Helper.getAxis(iterationPoint, axis);

                nextIsNewLineStart = false;

                continue;
            }
            // else we are here

            if (Helper.getAxis(iterationPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE) {
                lastAxisPosition = Helper.getAxis(iterationPoint, axis);
            } else {
                // form a new line
                RetinaPrimitive newPrimitive = RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, iterationPoint));
                newPrimitive.objectId = objectId;
                resultSingleLineDetectors.add(newPrimitive);

                nextIsNewLineStart = true;
            }
        }

        // form a new line for the last point
        ArrayRealVector lastPoint = pointPositions.get(pointPositions.size() - 1);

        if (!nextIsNewLineStart && Helper.getAxis(lastPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE) {
            RetinaPrimitive newPrimitive = RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, lastPoint));
            newPrimitive.objectId = objectId;
            resultSingleLineDetectors.add(newPrimitive);
        }

        return resultSingleLineDetectors;
    }

    // TODO< belongs into dedicated helper >
    static private class Helper {

//        private static boolean isDistanceBetweenPositionsBelow(ArrayRealVector a, ArrayRealVector b, double maxDistance) {
//            //return a.subtract(b).getNorm() < maxDistance;
//        }

        private static double getAxis(ArrayRealVector vector, EnumAxis axis) {
            final double[] dr = vector.getDataRef();
            if (axis == EnumAxis.X) {
                return dr[0];
            } else {
                return dr[1];
            }
        }
    }


    private static class VectorComperatorByAxis implements Comparator<ArrayRealVector> {

        public VectorComperatorByAxis(EnumAxis axis) {
            this.axis = axis;
        }

        @Override
        public int compare(ArrayRealVector a, ArrayRealVector b) {
            if (Helper.getAxis(a, axis) > Helper.getAxis(b, axis)) {
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

    private Random random = new RandomAdaptor(new MersenneTwister()); // new Random();

    // each cell contains the incides of the points/samples inside the accelerationMap
    private SpatialListMap2d<Integer> accelerationMap;

    private IntBooleanHashMap accelerationMapCellUsed;

    public double maximalDistanceOfPositions;

    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<RetinaPrimitive> outputLineDetectorConnector;
}
