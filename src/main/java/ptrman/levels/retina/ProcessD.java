package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.math.DistinctUtility;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static java.util.Collections.sort;
import static ptrman.math.ArrayRealVectorHelper.getScaled;

// TODO< remove detectors which are removable which have a activation less than <constant> * sumofAllActivations >
/**
 * detects lines
 * 
 * forms line hypothesis and tries to strengthen it
 * uses the method of the least squares to fit the potential lines
 * each line detector either will survive or decay if it doesn't receive enought fitting points
 * 
 */
public class ProcessD {
    // just for debugging as a flag, is actually true
    public final static boolean ENABLELOCKING = false;

    
    private static class LineDetectorWithMultiplePoints {
        public LineDetectorWithMultiplePoints(List<Integer> integratedSampleIndices) {
            this.integratedSampleIndices = integratedSampleIndices;
        }
        
        
        public List<Integer> integratedSampleIndices;
        
        public double m, n;
        
        public double mse = 0.0f;
        
        public boolean isLocked = false; // has the detector received enought activation so it stays?
        
        public boolean doesContainSampleIndex(int index)
        {
            return integratedSampleIndices.contains(index);
        }
        
        public double getActivation() {
            return integratedSampleIndices.size() + (Parameters.getProcessdMaxMse() - mse)*Parameters.getProcessdLockingActivationScale();
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
    }
    
    /**
     * 
     * \param samples doesn't need to be filtered for endo/exosceleton points, it does it itself
     * 
     * \return only the surviving line segments
     */
    public List<RetinaPrimitive> detectLines(List<ProcessA.Sample> samples) {
        final float SAMPLECOUNTLINEDETECTORMULTIPLIER = 3.5f;

        List<LineDetectorWithMultiplePoints> multiplePointsLineDetector = new ArrayList<>();

        List<ProcessA.Sample> workingSamples = filterEndosceletonPoints(samples);

        if( workingSamples.isEmpty() ) {
            return new ArrayList<>();
        }

        for( int counter = 0; counter < Math.round((float)samples.size()*SAMPLECOUNTLINEDETECTORMULTIPLIER); counter++ ) {
            {
                int centerPointIndex;
                RegressionForLineResult regressionResult;
                
                // to form a new line detector choose one point at random and chose n points in the neighborhood
                // this increases the chances that it lies on a (small) line
                centerPointIndex = random.nextInt(workingSamples.size());
                
                List<Integer> chosenPointIndices = new ArrayList<>();
                chosenPointIndices.add(centerPointIndex);
                
                // modifies chosenPointIndices
                choosePointIndicesInsideRadius(chosenPointIndices.get(0), chosenPointIndices, workingSamples, HardParameters.ProcessD.EARLYCANDIDATECOUNT-1);
                
                if( chosenPointIndices.size() < 2 ) {
                    // ignore potential detectors with less than two points
                    continue;
                }
                
                
                // create new line detector
                LineDetectorWithMultiplePoints createdLineDetector = new LineDetectorWithMultiplePoints(chosenPointIndices);
                

                
                List<ArrayRealVector> positionsOfSamples = getPositionsOfSamplesOfDetector(createdLineDetector, workingSamples);
            
                regressionResult = calcRegressionForPoints(positionsOfSamples);
                
                Assert.Assert(createdLineDetector.integratedSampleIndices.size() >= 2, "");
                // the regression mse is not defined if it are only two points
                if( createdLineDetector.integratedSampleIndices.size() == 2 ) {
                    // TODO< calculate m and n for the two point case >
                    
                    createdLineDetector.mse = 0.0f;
                    
                    createdLineDetector.n = regressionResult.n;
                    createdLineDetector.m = regressionResult.m;

                    lockDetectorIfItHasEnoughtActivation(createdLineDetector);

                    multiplePointsLineDetector.add(createdLineDetector);
                }
                else {
                    if( regressionResult.mse < Parameters.getProcessdMaxMse() ) {
                        createdLineDetector.mse = regressionResult.mse;

                        createdLineDetector.n = regressionResult.n;
                        createdLineDetector.m = regressionResult.m;

                        lockDetectorIfItHasEnoughtActivation(createdLineDetector);

                        multiplePointsLineDetector.add(createdLineDetector);
                    }
                }
            }
            
            // try to include a random sample into the detectors
            if( false ) {
                int sampleIndex = random.nextInt(workingSamples.size());

                // try to integrate the current sample into line(s)
                tryToIntegratePointIntoAllLineDetectors(sampleIndex, multiplePointsLineDetector, workingSamples);
            }
        }
        
        // delete all detectors for which the activation was not enought
        deleteMultiPointDetectorsWhereActiviationIsInsuficient(multiplePointsLineDetector);
        
        // split the detectors into one or many lines
        List<RetinaPrimitive> resultSingleDetectors = splitDetectorsIntoLines(multiplePointsLineDetector, samples);
        
        return resultSingleDetectors;
    }
    
    // TODO< use acceleration datastruction for the points >
    /**
     * modifies alreadyIntegratedPointIndices and returns result in it
     *
     * \param centerPointIndex
     * \param alreadyIntegratedPointIndices
     * \param workingSamples
     * \param count 
     */
    private void choosePointIndicesInsideRadius(int centerPointIndex, List<Integer> alreadyIntegratedPointIndices, List<ProcessA.Sample> workingSamples, int count) {
        final int MAXTRIES = 20;

        int tryCounter;
        
        ArrayRealVector centerPosition = workingSamples.get(centerPointIndex).position;

        tryCounter = 0;
        for( int counter = 0; counter < count; ) {
            int chosenPointIndex;
            List<Integer> chosenPointIndexAsList;
            
            tryCounter++;
            
            chosenPointIndexAsList = DistinctUtility.getDisjuctNumbersTo(random, alreadyIntegratedPointIndices, 1, workingSamples.size());
            Assert.Assert(chosenPointIndexAsList.size() == 1, "");
            chosenPointIndex = chosenPointIndexAsList.get(0);
            
            // check if it is inside radius
            ArrayRealVector chosenPointPosition = workingSamples.get(chosenPointIndex).position;
            
            if( Helper.isDistanceBetweenPositionsBelow(centerPosition, chosenPointPosition, HardParameters.ProcessD.EARLYCANDIDATEMAXDISTANCE) ) {
                alreadyIntegratedPointIndices.add(chosenPointIndex);
                
                // we have one more point, so increment counter
                counter++;
                tryCounter = 0;
                continue;
            }
            
            if( tryCounter >= MAXTRIES ) {
                return;
            }
        }
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
    
    
    private static void tryToIntegratePointIntoAllLineDetectors(int sampleIndex, List<LineDetectorWithMultiplePoints> multiplePointsLineDetector, List<ProcessA.Sample> workingSamples) {
        for( LineDetectorWithMultiplePoints iteratorDetector : multiplePointsLineDetector ) {
            ProcessA.Sample currentSample = workingSamples.get(sampleIndex);

            if( iteratorDetector.doesContainSampleIndex(sampleIndex) ) {
                continue;
            }
            // else we are here

            List<ArrayRealVector> positionsOfSamples = getPositionsOfSamplesOfDetector(iteratorDetector, workingSamples);
            positionsOfSamples.add(currentSample.position);

            RegressionForLineResult regressionResult = calcRegressionForPoints(positionsOfSamples);
            
            if( regressionResult.mse < Parameters.getProcessdMaxMse() ) {
                iteratorDetector.mse = regressionResult.mse;

                iteratorDetector.integratedSampleIndices.add(sampleIndex);

                iteratorDetector.n = regressionResult.n;
                iteratorDetector.m = regressionResult.m;

                lockDetectorIfItHasEnoughtActivation(iteratorDetector);
            }
        }
    }
    
    private static List<ArrayRealVector> getPositionsOfSamplesOfDetector(LineDetectorWithMultiplePoints detector, List<ProcessA.Sample> workingSamples) {
        List<ArrayRealVector> resultPositions = new ArrayList<>();
        
        for( int iterationSampleIndex : detector.integratedSampleIndices ) {
            ProcessA.Sample currentSample = workingSamples.get(iterationSampleIndex);
            resultPositions.add(currentSample.position);
        }
        
        return resultPositions;
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
    
    private static List<RetinaPrimitive> splitDetectorsIntoLines(List<LineDetectorWithMultiplePoints> lineDetectorsWithMultiplePoints, List<ProcessA.Sample> samples) {
        List<RetinaPrimitive> result;
        
        result = new ArrayList<>();
        
        for( LineDetectorWithMultiplePoints iterationDetector : lineDetectorsWithMultiplePoints ) {
            result.addAll(splitDetectorIntoLines(iterationDetector, samples));
        }
        
        return result;
    }
    
    private static List<RetinaPrimitive> splitDetectorIntoLines(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints, List<ProcessA.Sample> samples) {
        if( lineDetectorWithMultiplePoints.isYAxisSingularity() ) {
            // handle the special case where its all on one x coordinate

            List<ArrayRealVector> samplePositions = new ArrayList<>();
            
            for( int iterationSampleIndex : lineDetectorWithMultiplePoints.integratedSampleIndices ) {
                samplePositions.add(samples.get(iterationSampleIndex).position);
            }
            
            sort(samplePositions, new VectorComperatorByAxis(EnumAxis.Y));
            
            return clusterPointsFromLinedetectorToLinedetectors(samplePositions, EnumAxis.Y);
        }
        else {
            List<ArrayRealVector> projectedPointPositions = new ArrayList<>();
            
            // first sort all points after the x position
            // and then "cluster" the lines after the distance between succeeding points
            
            // project
            for( int iterationSampleIndex : lineDetectorWithMultiplePoints.integratedSampleIndices ) {
                ArrayRealVector projectedSamplePosition = lineDetectorWithMultiplePoints.projectPointOntoLine(samples.get(iterationSampleIndex).position);

                projectedPointPositions.add(projectedSamplePosition);
            }

            sort(projectedPointPositions, new VectorComperatorByAxis(EnumAxis.X));

            return clusterPointsFromLinedetectorToLinedetectors(projectedPointPositions, EnumAxis.X);
        }
    }
    
    
    private static List<RetinaPrimitive> clusterPointsFromLinedetectorToLinedetectors(List<ArrayRealVector> pointPositions, EnumAxis axis) {
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
                resultSingleLineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, iterationPoint)));

                nextIsNewLineStart = true;
            }
        }

        // form a new line for the last point
        ArrayRealVector lastPoint = pointPositions.get(pointPositions.size()-1);

        if( !nextIsNewLineStart && Helper.getAxis(lastPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE ) {
            resultSingleLineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, lastPoint)));
        }
        
        return resultSingleLineDetectors;
    }
    
    private static void deleteMultiPointDetectorsWhereActiviationIsInsuficient(List<LineDetectorWithMultiplePoints> lineDetectors) {
        int detectorI;
        
        if( !ENABLELOCKING ) {
            return;
        }
        
        for( detectorI = lineDetectors.size()-1; detectorI >= 0; detectorI-- ) {
            LineDetectorWithMultiplePoints discardCandidate;
            
            discardCandidate = lineDetectors.get(detectorI);
            
            if( !discardCandidate.isLocked ) {
                lineDetectors.remove(detectorI);
            }
        }
    }
    
    private static void lockDetectorIfItHasEnoughtActivation(LineDetectorWithMultiplePoints detector) {
        detector.isLocked |= detector.getActivation() > Parameters.getProcessdLockingActivation();
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
    
    public Random random = new Random();
    
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
}
