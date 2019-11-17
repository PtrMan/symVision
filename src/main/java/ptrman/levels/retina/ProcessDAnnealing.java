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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.misc.Assert;

import java.util.*;

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
    // candidates which are annealed
    public List<LineDetectorWithMultiplePoints> annealedCandidates = new ArrayList<>();

    // 5 for testing
    public int anealedCandidatesMaxCount = 10; // maximal number of line segments which are considered


    private Vector2d<Integer> imageSize;

    private int gridcellSize = 8;

    private Random random = new RandomAdaptor(new MersenneTwister()); // new Random();

    public double maximalDistanceOfPositions;

    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<RetinaPrimitive> outputLineDetectorConnector;


    public int widenSamplesPerTrial = 10; // how many points are considered when doing a widening step?
    public double widenSampleMaxDistance = 3.0; // maximal distance of projected position to line to actual position

    public Random rng = new Random();


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
        //accelerationMap = new SpatialListMap2d<>(new Vector2d<>(imageSize.x + gridcellSize, imageSize.y + gridcellSize * 2), gridcellSize);

        //accelerationMapCellUsed = new IntBooleanHashMap();
    }

    @Override
    public void processData() {
        processData(1f);
    }


    // sorts annealedCandidates by activation and throws items with to low activation away
    public void sortByActivationAndThrowAway() {
        annealedCandidates.sort((a, b) -> a.getActivation() < b.getActivation() ? 1 : -1);

        // limit size
        while(annealedCandidates.size() > anealedCandidatesMaxCount) {
            annealedCandidates.remove(anealedCandidatesMaxCount-1);
        }
    }

    // tries to sample a new line candidate
    public void sampleNew() {
        final double maxLength = Math.sqrt(squaredDistance(new double[]{imageSize.x, imageSize.y})); // max length of line

        List<LineDetectorWithMultiplePoints> multiplePointsLineDetector = new ArrayList<>();

        final List<ProcessA.Sample> workingSamples = inputSampleConnector.getWorkspace();

        if (workingSamples.isEmpty()) {
            return;
        }

        // pick out random points
        int sampleIndex = 0; // NOTE< index in endosceletonPoint / workingSamples >
        IntArrayList allCandidateSampleIndices = new IntArrayList();
        for (final ProcessA.Sample iterationSample : workingSamples) {
            allCandidateSampleIndices.add(sampleIndex);
            sampleIndex++;
        }


        IntList chosenCandidateSampleIndices = getRandomElements(allCandidateSampleIndices, 3, random);
        List<ProcessA.Sample> selectedSamples = getSamplesByIndices(workingSamples, chosenCandidateSampleIndices);

        tryCreateMultiLineDetector(maxLength, chosenCandidateSampleIndices, selectedSamples);

    }

    private void tryCreateMultiLineDetector(
            double maxLength,
            IntList chosenCandidateSampleIndices, List<ProcessA.Sample> selectedSamples) {
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

        final RegressionForLineResult regressionResult = calcRegressionResultOfSamples(selectedSamples);

        if (regressionResult.mse > Parameters.getProcessdMaxMse()) {
            return;
        }
        // else we are here


        // create new line detector
        LineDetectorWithMultiplePoints createdLineDetector = new LineDetectorWithMultiplePoints();
        createdLineDetector.integratedSampleIndices = chosenCandidateSampleIndices;
        createdLineDetector.samples = selectedSamples;

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
            annealedCandidates.addAll(Arrays.asList(new LineDetectorWithMultiplePoints[]{createdLineDetector}));
        }
    }

    private static RegressionForLineResult calcRegressionResultOfSamples(List<ProcessA.Sample> samples) {
        final List<ArrayRealVector> positionsOfSamples = getPositionsOfSamples(samples);
        final RegressionForLineResult regressionResult = calcRegressionForPoints(positionsOfSamples);
        return regressionResult;
    }

    public void processData(float throttle) {

        for(int i = 0; i < 5; i++) {
            sampleNew();
            tryWiden();
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

    /**
     * tries to add points to existing lines
     */
    public void tryWiden() {
        for (LineDetectorWithMultiplePoints iLinedetector : annealedCandidates) {

            // sample samples and project to line, check distance if it is below threshold
            for (int iSamplingAttempt=0;iSamplingAttempt<widenSamplesPerTrial;iSamplingAttempt++) {
                int sampleIdx = rng.nextInt(inputSampleConnector.workspace.size());

                // * project on line, check
                ProcessA.Sample sample = inputSampleConnector.workspace.get(sampleIdx);
                ArrayRealVector projectedPosition = iLinedetector.projectPointOntoLine(sample.position);
                double dist = calcDistance(sample.position, projectedPosition);
                if (dist > widenSampleMaxDistance) {
                    continue;
                }

                // * add to line
                iLinedetector.samples.add(sample);

                // * recompute line and mse
                RegressionForLineResult regressionResult = calcRegressionResultOfSamples(iLinedetector.samples);
                iLinedetector.m = regressionResult.m;
                iLinedetector.n = regressionResult.n;
                iLinedetector.mse = regressionResult.mse;
            }
        }
    }

    @Override
    public void postProcessData() {

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

    private static List<ProcessA.Sample> getSamplesByIndices(final List<ProcessA.Sample> samples, final IntList indices) {
        List<ProcessA.Sample> resultPositions = new ArrayList<>(indices.size());
        indices.forEach(index -> resultPositions.add(samples.get(index)));
        return resultPositions;
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

}
