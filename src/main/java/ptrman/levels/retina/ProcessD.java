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
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.misc.Assert;

import java.util.*;

import static ptrman.levels.retina.LineDetectorWithMultiplePoints.real;
import static ptrman.math.ArrayRealVectorHelper.distance;
import static ptrman.math.ArrayRealVectorHelper.getAverage;
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
public class ProcessD implements IProcess {
    // candidates which are annealed
    public final List<LineDetectorWithMultiplePoints> annealedCandidates = new ArrayList<>();

    // 5 for testing
    public final int anealedCandidatesMaxCount = 40; // maximal number of line segments which are considered


    private Vector2d<Integer> imageSize;

    public double maximalDistanceOfPositions;

    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<RetinaPrimitive> outputLineDetectorConnector;


    public final int widenSamplesPerTrial = 10; // how many points are considered when doing a widening step?
    public final double widenSampleMaxDistance = 4.0; // maximal distance of projected position to line to actual position

    public boolean onlyEndoskeleton = false; // only work with samples which belong to endoskeleton?

    public double lineDetectorInitialXStep = 1.0 / 10.0; // initial step size of a line detector along the activation function

    public final Random rng = new RandomAdaptor(new MersenneTwister());


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
    }

    @Override
    public void processData() {
        processData(1f);
    }


    // sorts annealedCandidates by activation and throws items with to low activation away
    public void sortByActivationAndThrowAway() {
        annealedCandidates.sort((a, b) -> (a==b) ? 0 : a.calcActivation() < b.calcActivation() ? 1 : -1);

        // limit size
        while(annealedCandidates.size() > anealedCandidatesMaxCount) {
            annealedCandidates.get(anealedCandidatesMaxCount-1).cleanup(); // decrement ref count to free up samples
            annealedCandidates.remove(anealedCandidatesMaxCount-1);
        }
    }

    public void removeCandidatesBelowActivation(double threshold) {
        for(int idx=annealedCandidates.size()-1;idx >= 0;idx--) {
            double activation = annealedCandidates.get(idx).calcActivation();
            if (activation < threshold) {
                annealedCandidates.remove(idx);
            }
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
            boolean onlyAddEndoskeletonEnable = !(onlyEndoskeleton && iterationSample.type != ProcessA.Sample.EnumType.ENDOSCELETON);
            boolean isReferenced = iterationSample.refCount != 0;
            if(!isReferenced && onlyAddEndoskeletonEnable ) {
                allCandidateSampleIndices.add(sampleIndex);
            }
            sampleIndex++;
        }


        IntList chosenCandidateSampleIndices = getRandomElements(allCandidateSampleIndices, 3, rng);
        List<ProcessA.Sample> selectedSamples = getSamplesByIndices(workingSamples, chosenCandidateSampleIndices);

        tryCreateMultiLineDetector(maxLength, chosenCandidateSampleIndices, selectedSamples);

    }

    public double processDNumberOfPointsToActivationScale = 0.15; // how much does a point improve the scaling

    private void tryCreateMultiLineDetector(
            double maxLength,
            IntList chosenCandidateSampleIndices, List<ProcessA.Sample> selectedSamples) {
        // commented check because we don't assume object id's anymore (because it was from Phaeaco for solving BP's)
        //final boolean doAllSamplesHaveId = doAllSamplesHaveObjectId(selectedSamples);
        //if (!doAllSamplesHaveId) {
        //    return;
        //}

        if (selectedSamples.size() == 0) {
            return; // special case
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

        if(chosenCandidateSampleIndices.size() <= 2) { // the regression mse is not defined if it are only two points
            return; // only create detector if we have at least three samples
        }


        // create new line detector
        LineDetectorWithMultiplePoints createdLineDetector = new LineDetectorWithMultiplePoints(lineDetectorInitialXStep);
        createdLineDetector.integratedSampleIndices = chosenCandidateSampleIndices;
        createdLineDetector.samples = selectedSamples;

        Assert.Assert(areObjectIdsTheSameOfSamples(selectedSamples), "");
        createdLineDetector.commonObjectId = selectedSamples.get(0).objectId;


        createdLineDetector.recalcConf(); // necessary

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

        createdLineDetector.x += createdLineDetector.samples.size() * processDNumberOfPointsToActivationScale; // more fitting points -> better activation
        createdLineDetector.x += (Parameters.getProcessdMaxMse() - createdLineDetector.mse) * Parameters.getProcessdLockingActivationScale(); // better mse -> better activation

        if (addCreatedLineDetector) {
            for(ProcessA.Sample iSample : createdLineDetector.samples) {
                iSample.refCount++;
            }

            annealedCandidates.addAll(Collections.singletonList(createdLineDetector));
        }
    }

    private static RegressionForLineResult calcRegressionResultOfSamples(Iterable<ProcessA.Sample> samples) {
        return calcRegressionForPoints(getPositionsOfSamples(samples));
    }

    public void processData(float throttle) {

        for(int i = 0; i < 9; i++) {
            step();
        }

        /* TODO 16.11.2019 activate this code again because it is necessary as the last step
        commitLineDetectors();
         */
    }

    /**
     * splits and pushes lines into the output connector
     *
     * is the last step when processing a frame
     */
    public void commitLineDetectors() {
        // split the detectors into one or many lines
        final List<RetinaPrimitive> resultSingleDetectors = splitDetectorsIntoLines(annealedCandidates);

        for (final RetinaPrimitive iterationPrimitive : resultSingleDetectors) {
            outputLineDetectorConnector.add(iterationPrimitive);
        }
    }

    private static List<RetinaPrimitive> splitDetectorsIntoLines(Iterable<LineDetectorWithMultiplePoints> lineDetectorsWithMultiplePoints) {
        List<RetinaPrimitive> result;

        result = new ArrayList<>();

        for (LineDetectorWithMultiplePoints iterationDetector : lineDetectorsWithMultiplePoints) {
            result.addAll(splitDetectorIntoLines(iterationDetector));
        }

        return result;
    }

    /**
     * processing step
     */
    public void step() {
        sampleNew();
        tryWiden();
        detectorsHarden();
        detectorsFadeActivation();
        //sortByActivationAndThrowAway();
        removeCandidatesBelowActivation(0.05);
    }

    public double processDHardenDetectorThreshold = 0.6;
    public double processDHardenDetectorFactor = 0.1;

    public void detectorsHarden() {
        // make fading harden when the x value of the activation exceeds a threshold
        // this is done to prefer to keep good detectors
        // https://www.foundalis.com/res/Generalization_of_Hebbian_Learning_and_Categorization.pdf
        // "
        //    However, when x exceeds a threshold that is just before the maximum value 1,
        //    the number of discrete steps along the x-axis  is  increased  somewhat
        //    (the  resolution  of  segmenting  the x-axis grows). This implies that the subsequent
        //    fading of the activation will become slower, because x will have more backward steps to
        //    traverse along the x-axis. The meaning of this  change  is  that  associations  that  are
        //    well  established  should  become  progressively  harder  to  fade,  after  repeated
        //    confirmations of their correctness. The amount by which the number of steps is increased
        //    is a parameter of the system.
        // "
        for (LineDetectorWithMultiplePoints iDetector : annealedCandidates) {
            if (!iDetector.isHardened && iDetector.calcActivationX() > processDHardenDetectorThreshold) {
                iDetector.isHardened = true;
                iDetector.xStep *= processDHardenDetectorFactor; // make it harder to decay
            }
        }
    }

    /**
     * fades the activation of all detectors as described in https://www.foundalis.com/res/Generalization_of_Hebbian_Learning_and_Categorization.pdf page 4
     */
    public void detectorsFadeActivation() {
        for (LineDetectorWithMultiplePoints iDetector : annealedCandidates) {
            iDetector.xDecayDelta -= iDetector.xStep;
        }
    }

    /**
     * tries to add points to existing lines
     */
    public void tryWiden() {
        for (LineDetectorWithMultiplePoints iLinedetector : annealedCandidates) {

            // sample samples and project to line, check distance if it is below threshold
            for (int iSamplingAttempt=0;iSamplingAttempt<widenSamplesPerTrial;iSamplingAttempt++) {
                int sampleIdx = rng.nextInt(inputSampleConnector.workspace.size());

                ProcessA.Sample sample = inputSampleConnector.workspace.get(sampleIdx);
                if(onlyEndoskeleton && sample.type != ProcessA.Sample.EnumType.ENDOSCELETON) {
                    continue;
                }
                if (sample.refCount > 0) {
                    continue; // sample is already in use
                }

                // * project on line, check
                if (iLinedetector.isYAxisSingularity()) {
                    continue; // ignore because we can't project
                }
                ArrayRealVector projectedPosition = iLinedetector.projectPointOntoLine(sample.position);
                double dist = distance(sample.position, projectedPosition);
                if (dist > widenSampleMaxDistance) {
                    continue;
                }

                // * add to line
                iLinedetector.samples.add(sample);
                sample.refCount++;

                // * recompute line and mse
                RegressionForLineResult regressionResult = calcRegressionResultOfSamples(iLinedetector.samples);
                iLinedetector.m = regressionResult.m;
                iLinedetector.n = regressionResult.n;
                iLinedetector.mse = regressionResult.mse;

                iLinedetector.x = 0.0;
                iLinedetector.x += iLinedetector.samples.size() * processDNumberOfPointsToActivationScale; // more fitting points -> better activation
                iLinedetector.x += (Parameters.getProcessdMaxMse() - iLinedetector.mse) * Parameters.getProcessdLockingActivationScale(); // better mse -> better activation

                // * recompute conf
                iLinedetector.recalcConf();
            }
        }
    }

    @Override
    public void postProcessData() {

    }

    private static boolean doAllSamplesHaveObjectId(final Iterable<ProcessA.Sample> samples) {
        for (final ProcessA.Sample iterationSamples : samples) {
            if (!iterationSamples.isObjectIdValid()) {
                return false;
            }
        }

        return true;
    }

    private static boolean areObjectIdsTheSameOfSamples(final List<ProcessA.Sample> samples) {
        return true;
        /*
        Assert.Assert(samples.get(0).isObjectIdValid(), "");

        final int objectId = samples.get(0).objectId;

        for (final ProcessA.Sample iterationSamples : samples) {
            Assert.Assert(iterationSamples.isObjectIdValid(), "");

            if (iterationSamples.objectId != objectId) {
                return false;
            }
        }

        return true;

         */
    }

    /** TODO stream */
    private static List<ArrayRealVector> getPositionsOfSamples(final Iterable<ProcessA.Sample> samples) {
        List<ArrayRealVector> resultPositions = new ArrayList<>();

        for (final ProcessA.Sample iterationSample : samples)
            resultPositions.add(real(iterationSample.position));

        return resultPositions;
    }

    private static double getMaximalDistanceOfPositionsTo(final Iterable<ArrayRealVector> positions, final ArrayRealVector comparePosition) {
        double maxDistance = 0.0;

        for (final ArrayRealVector iterationPosition : positions) {
            final double currentDistance = iterationPosition.getDistance(comparePosition);
            maxDistance = java.lang.Math.max(maxDistance, currentDistance);
        }

        return maxDistance;
    }

    private static List<ProcessA.Sample> getSamplesByIndices(final List<ProcessA.Sample> samples, final IntIterable indices) {
        List<ProcessA.Sample> resultPositions = new ArrayList<>(indices.size());
        indices.forEach(index -> resultPositions.add(samples.get(index)));
        return resultPositions;
    }

    /**
     * works by counting the "overlapping" pixel coordinates, chooses the axis with the less overlappings
     */
    private static RegressionForLineResult calcRegressionForPoints(Iterable<ArrayRealVector> positions) {

        int overlappingPixelsOnX = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.X);
        int overlappingPixelsOnY = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.Y);

        SimpleRegression regression = new SimpleRegression();

        RegressionForLineResult regressionResultForLine = new RegressionForLineResult();

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

    private static int calcCountOfOverlappingPixelsForAxis(Iterable<ArrayRealVector> positions, EnumAxis axis) {
        double maxCoordinatOnAxis = getMaximalCoordinateForPoints(positions, axis);
        int arraysizeOfDimension = Math.round((float) maxCoordinatOnAxis) + 1;
        int[] dimensionCounter = new int[arraysizeOfDimension];

        for (ArrayRealVector iterationPosition : positions) {
            int dimensionCounterIndex = Math.round((float) Helper.getAxis(iterationPosition, axis));

            dimensionCounter[dimensionCounterIndex]++;
        }

        // count the "rows" where the count is greater than 1
        int overlappingCounter = 0;

        for (int i : dimensionCounter) {
            if (i > 1) {
                overlappingCounter++;
            }
        }

        return overlappingCounter;
    }

    // used to calculate the arraysize
    private static double getMaximalCoordinateForPoints(Iterable<ArrayRealVector> positions, EnumAxis axis) {
        double max = 0;

        for (ArrayRealVector iterationPosition : positions) {
            max = Math.max(max, Helper.getAxis(iterationPosition, axis));
        }

        return max;
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

        return lineDetectorWithMultiplePoints.isYAxisSingularity() ? clusterPointsFromLinedetectorToLinedetectors(lineDetectorWithMultiplePoints.commonObjectId, lineDetectorWithMultiplePoints.cachedConf, sortedSamplePositions, EnumAxis.Y) : clusterPointsFromLinedetectorToLinedetectors(lineDetectorWithMultiplePoints.commonObjectId, lineDetectorWithMultiplePoints.cachedConf, sortedSamplePositions, EnumAxis.X);
    }


    private static List<RetinaPrimitive> clusterPointsFromLinedetectorToLinedetectors(final int objectId, final double conf, final List<ArrayRealVector> pointPositions, final EnumAxis axis) {
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
            newPrimitive.conf = conf;
            resultSingleLineDetectors.add(newPrimitive);
        }

        return resultSingleLineDetectors;
    }

    // TODO< belongs into dedicated helper >
    static private class Helper {
        private static double getAxis(ArrayRealVector vector, EnumAxis axis) {
            final double[] dr = vector.getDataRef();
            return axis == EnumAxis.X ? dr[0] : dr[1];
        }
    }


    private static class VectorComperatorByAxis implements Comparator<ArrayRealVector> {

        public VectorComperatorByAxis(EnumAxis axis) {
            this.axis = axis;
        }

        @Override
        public int compare(ArrayRealVector a, ArrayRealVector b) {
            return Double.compare(Helper.getAxis(a, axis), Helper.getAxis(b, axis));

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
