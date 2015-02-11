package RetinaLevel;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.dot;
import static Datastructures.Vector2d.FloatHelper.getLength;
import static Datastructures.Vector2d.FloatHelper.getScaled;
import static Datastructures.Vector2d.FloatHelper.sub;
import bpsolver.HardParameters;
import bpsolver.Parameters;
import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.Random;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import math.DistinctUtility;
import misc.Assert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

// TODO< remove detectors which are removable which have a activation less than <constant> * sumofAllActivations >
/**
 * detects lines
 * 
 * forms line hypothesis and tries to strengthen it
 * uses the method of the least squares to fit the potential lines
 * each line detector either will survive or decay if it doesn't receive enought fitting points
 * 
 */
public class ProcessD
{
    // just for debugging as a flag, is actually true
    public final static boolean ENABLELOCKING = false;

    
    private static class LineDetectorWithMultiplePoints
    {
        public LineDetectorWithMultiplePoints(ArrayList<Integer> integratedSampleIndices)
        {
            this.integratedSampleIndices = integratedSampleIndices;
        }
        
        
        public ArrayList<Integer> integratedSampleIndices = new ArrayList<>();
        
        public float m, n;
        
        public float mse = 0.0f;
        
        public boolean isLocked = false; // has the detector received enought activation so it stays?
        
        public boolean doesContainSampleIndex(int index)
        {
            return integratedSampleIndices.contains(index);
        }
        
        public float getActivation()
        {
            return (float)integratedSampleIndices.size() + (Parameters.getProcessdMaxMse() - mse)*Parameters.getProcessdLockingActivationScale();
        }
        
        public Vector2d<Float> projectPointOntoLine(Vector2d<Float> point)
        {
            if( isYAxisSingularity() )
            {
                // call isn't allowed
                throw new RuntimeException("internal error");
                //return projectPointOntoLineForSingular(point);
            }
            else
            {
                return projectPointOntoLineForNonsingular(point);
            }
        }
        
        /*private Vector2d<Float> projectPointOntoLineForSingular(Vector2d<Float> point)
        {
            return new Vector2d<Float>(point.x, horizontalOffset);
        }*/
        
        public Vector2d<Float> projectPointOntoLineForNonsingular(Vector2d<Float> point)
        {
            Vector2d<Float> lineDirection;
            Vector2d<Float> diffFromAToPoint;
            float dotResult;

            lineDirection = getNormalizedDirection();
            diffFromAToPoint = sub(point, new Vector2d<>(0.0f, n));
            dotResult = dot(lineDirection, diffFromAToPoint);

            return add(new Vector2d<>(0.0f, n), getScaled(lineDirection, dotResult));
        }
        
        private Vector2d<Float> getNormalizedDirection()
        {
            if( isYAxisSingularity() )
            {
                throw new RuntimeException("internal error");
                //return new Vector2d<Float>(0.0f, 1.0f);
            }
            else
            {
                return Vector2d.FloatHelper.normalize(new Vector2d<>(1.0f, m));
            }
        }
        
        // TODO< just simply test flag >
        public boolean isYAxisSingularity()
        {
            return Float.isInfinite(m);
        }
        
        public float getHorizontalOffset(ArrayList<ProcessA.Sample> samples)
        {
            int sampleIndex;
            
            Assert.Assert(isYAxisSingularity(), "");
            
            sampleIndex = integratedSampleIndices.get(0);
            return samples.get(sampleIndex).position.x;
        }
    }
    
    /**
     * 
     * \param samples doesn't need to be filtered for endo/exosceleton points, it does it itself
     * 
     * \return only the surviving line segments
     */
    public ArrayList<RetinaPrimitive> detectLines(ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<ProcessA.Sample> workingSamples;
        ArrayList<LineDetectorWithMultiplePoints> multiplePointsLineDetector;
        ArrayList<RetinaPrimitive> resultSingleDetectors;
        
        final float SAMPLECOUNTLINEDETECTORMULTIPLIER = 3.5f;
        
        multiplePointsLineDetector = new ArrayList<>();
        
        workingSamples = filterEndosceletonPoints(samples);
        
        int counter;
        
        for( counter = 0; counter < Math.round((float)samples.size()*SAMPLECOUNTLINEDETECTORMULTIPLIER); counter++ )
        {
            {
                int centerPointIndex;
                
                // to form a new line detector choose one point at random and chose n points in the neighborhood
                // this increases the chances that it lies on a (small) line
                centerPointIndex = random.nextInt(workingSamples.size());
                
                ArrayList<Integer> chosenPointIndices = new ArrayList<Integer>();
                chosenPointIndices.add(centerPointIndex);
                
                // modifies chosenPointIndices
                choosePointIndicesInsideRadius(chosenPointIndices.get(0), chosenPointIndices, workingSamples, HardParameters.ProcessD.EARLYCANDIDATECOUNT-1);
                
                if( chosenPointIndices.size() < 2 )
                {
                    // ignore potential detectors with less than two points
                    continue;
                }
                
                
                // create new line detector
                LineDetectorWithMultiplePoints createdLineDetector;

                
                createdLineDetector = new LineDetectorWithMultiplePoints(chosenPointIndices);
                
                ArrayList<Vector2d<Float>> positionsOfSamples;
                RegressionForLineResult regressionResult;
                
                positionsOfSamples = getPositionsOfSamplesOfDetector(createdLineDetector, workingSamples);
            
                regressionResult = calcRegressionForPoints(positionsOfSamples);
                
                Assert.Assert(createdLineDetector.integratedSampleIndices.size() >= 2, "");
                // the regression mse is not defined if it are only two points
                if( createdLineDetector.integratedSampleIndices.size() == 2 )
                {
                    // TODO< calculate m and n for the two point case >
                    
                    createdLineDetector.mse = 0.0f;
                    
                    createdLineDetector.n = regressionResult.n;
                    createdLineDetector.m = regressionResult.m;

                    lockDetectorIfItHasEnoughtActivation(createdLineDetector);

                    multiplePointsLineDetector.add(createdLineDetector);
                }
                else
                {
                    if( regressionResult.mse < Parameters.getProcessdMaxMse() )
                    {
                        createdLineDetector.mse = regressionResult.mse;

                        createdLineDetector.n = regressionResult.n;
                        createdLineDetector.m = regressionResult.m;

                        lockDetectorIfItHasEnoughtActivation(createdLineDetector);

                        multiplePointsLineDetector.add(createdLineDetector);
                    }
                }
            }
            
            // try to include a random sample into the detectors
            if( false )
            {
                int sampleIndex;

                sampleIndex = random.nextInt(workingSamples.size());


                // try to integrate the current sample into line(s)
                tryToIntegratePointIntoAllLineDetectors(sampleIndex, multiplePointsLineDetector, workingSamples);
            }
        }
        
        // delete all detectors for which the activation was not enought
        deleteMultiPointDetectorsWhereActiviationIsInsuficient(multiplePointsLineDetector);
        
        // split the detectors into one or many lines
        resultSingleDetectors = splitDetectorsIntoLines(multiplePointsLineDetector, samples);
        
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
    private void choosePointIndicesInsideRadius(int centerPointIndex, ArrayList<Integer> alreadyIntegratedPointIndices, ArrayList<ProcessA.Sample> workingSamples, int count)
    {
        final int MAXTRIES = 20;
        
        Vector2d<Integer> centerPositionAsInt;
        Vector2d<Float> centerPosition;
        int counter, tryCounter;
        
        centerPositionAsInt = workingSamples.get(centerPointIndex).position;
        centerPosition = Vector2d.ConverterHelper.convertIntVectorToFloat(centerPositionAsInt);
        
        tryCounter = 0;
        for( counter = 0; counter < count; )
        {
            int chosenPointIndex;
            ArrayList<Integer> chosenPointIndexAsList;
            Vector2d<Integer> chosenPointPositionAsInt;
            Vector2d<Float> chosenPointPosition;
            
            tryCounter++;
            
            chosenPointIndexAsList = DistinctUtility.getDisjuctNumbersTo(random, alreadyIntegratedPointIndices, 1, workingSamples.size());
            Assert.Assert(chosenPointIndexAsList.size() == 1, "");
            chosenPointIndex = chosenPointIndexAsList.get(0);
            
            // check if it is inside radius
            chosenPointPositionAsInt = workingSamples.get(chosenPointIndex).position;
            chosenPointPosition = Vector2d.ConverterHelper.convertIntVectorToFloat(chosenPointPositionAsInt);
            
            if( Helper.isDistanceBetweenPositionsBelow(centerPosition, chosenPointPosition, HardParameters.ProcessD.EARLYCANDIDATEMAXDISTANCE) )
            {
                alreadyIntegratedPointIndices.add(chosenPointIndex);
                
                // we have one more point, so increment counter
                counter++;
                tryCounter = 0;
                continue;
            }
            
            if( tryCounter >= MAXTRIES )
            {
                return;
            }
        }
    }
    
    // TODO< belongs into dedicated helper >
    static private class Helper
    {
        private static boolean isDistanceBetweenPositionsBelow(Vector2d<Float> a, Vector2d<Float> b, float maxDistance)
        {
            return getLength(sub(a, b)) < maxDistance;
        }
        
        private static float getAxis(Vector2d<Float> vector, EnumAxis axis)
        {
            if( axis == EnumAxis.X )
            {
                return vector.x;
            }
            else
            {
                return vector.y;
            }
        }
    }
    
    
    private static void tryToIntegratePointIntoAllLineDetectors(int sampleIndex, ArrayList<LineDetectorWithMultiplePoints> multiplePointsLineDetector, ArrayList<ProcessA.Sample> workingSamples) {
        for( LineDetectorWithMultiplePoints iteratorDetector : multiplePointsLineDetector )
        {
            ArrayList<Vector2d<Float>> positionsOfSamples;
            RegressionForLineResult regressionResult;
            ProcessA.Sample currentSample;

            currentSample = workingSamples.get(sampleIndex);

            if( iteratorDetector.doesContainSampleIndex(sampleIndex) )
            {
                continue;
            }
            // else we are here
            
            positionsOfSamples = getPositionsOfSamplesOfDetector(iteratorDetector, workingSamples);
            positionsOfSamples.add(Vector2d.ConverterHelper.convertIntVectorToFloat(currentSample.position));
            
            regressionResult = calcRegressionForPoints(positionsOfSamples);
            
            if( regressionResult.mse < Parameters.getProcessdMaxMse() )
            {
                iteratorDetector.mse = regressionResult.mse;

                iteratorDetector.integratedSampleIndices.add(sampleIndex);

                iteratorDetector.n = regressionResult.n;
                iteratorDetector.m = regressionResult.m;

                lockDetectorIfItHasEnoughtActivation(iteratorDetector);
            }
        }
    }
    
    private static ArrayList<Vector2d<Float>> getPositionsOfSamplesOfDetector(LineDetectorWithMultiplePoints detector, ArrayList<ProcessA.Sample> workingSamples)
    {
        ArrayList<Vector2d<Float>> resultPositions;
        
        resultPositions = new ArrayList<>();
        
        for( int iterationSampleIndex : detector.integratedSampleIndices )
        {
            ProcessA.Sample currentSample;
            Vector2d<Float> convertedPosition;
            
            currentSample = workingSamples.get(iterationSampleIndex);
            convertedPosition = Vector2d.ConverterHelper.convertIntVectorToFloat(currentSample.position);
            resultPositions.add(convertedPosition);
        }
        
        return resultPositions;
    }
    
    /**
     * works by counting the "overlapping" pixel coordinates, chooses the axis with the less overlappings
     *  
     */
    private static RegressionForLineResult calcRegressionForPoints(ArrayList<Vector2d<Float>> positions)
    {
        SimpleRegression regression;
        
        int overlappingPixelsOnX, overlappingPixelsOnY;
        
        RegressionForLineResult regressionResultForLine;
        
        overlappingPixelsOnX = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.X);
        overlappingPixelsOnY = calcCountOfOverlappingPixelsForAxis(positions, EnumAxis.Y);
        
        regression = new SimpleRegression();
        
        regressionResultForLine = new RegressionForLineResult();
        
        if( overlappingPixelsOnX <= overlappingPixelsOnY )
        {
            // regression on x axis
            
            for( Vector2d<Float> iterationPosition : positions )
            {
                regression.addData(iterationPosition.x, iterationPosition.y);
            }
            
            regressionResultForLine.mse = (float)regression.getMeanSquareError();
            regressionResultForLine.n = (float)regression.getIntercept();
            regressionResultForLine.m = (float)regression.getSlope();
        }
        else
        {
            float regressionM, n, m, regressionN;
            Vector2d<Float> pointOnRegressionLine;
            
            // regression on y axis
            // we switch x and y and calculate m and n from the regression result
            
            for( Vector2d<Float> iterationPosition : positions )
            {
                regression.addData(iterationPosition.y, iterationPosition.x);
            }
            
            // calculate m and n
            regressionM = (float)regression.getSlope();
            regressionN = (float)regression.getIntercept();
            
            m = 1.0f/regressionM;
            pointOnRegressionLine = new Vector2d<>(regressionN, 0.0f);
            n = pointOnRegressionLine.y - m * pointOnRegressionLine.x;
            
            regressionResultForLine.mse = (float)regression.getMeanSquareError();
            regressionResultForLine.n = n;
            regressionResultForLine.m = m;
        }
        
        return regressionResultForLine;
    }
    
    private static int calcCountOfOverlappingPixelsForAxis(ArrayList<Vector2d<Float>> positions, EnumAxis axis) {
        float maxCoordinatOnAxis;
        int arraysizeOfDimension;
        int[] dimensionCounter;
        int overlappingCounter;
        int arrayI;
        
        overlappingCounter = 0;
        
        maxCoordinatOnAxis = getMaximalCoordinateForPoints(positions, axis);
        arraysizeOfDimension = Math.round(maxCoordinatOnAxis)+1;
        dimensionCounter = new int[arraysizeOfDimension];
        
        for( Vector2d<Float> iterationPosition : positions )
        {
            int dimensionCounterIndex;
            
            dimensionCounterIndex = Math.round(Helper.getAxis(iterationPosition, axis));
            
            dimensionCounter[dimensionCounterIndex]++;
        }
        
        // count the "rows" where the count is greater than 1
        for( arrayI = 0; arrayI < dimensionCounter.length; arrayI++ )
        {
            if( dimensionCounter[arrayI] > 1 )
            {
                overlappingCounter++;
            }
        }
        
        return overlappingCounter;
    }
    
    // used to calculate the arraysize
    private static float getMaximalCoordinateForPoints(ArrayList<Vector2d<Float>> positions, EnumAxis axis)
    {
        float max;
        
        max = 0;
        
        for( Vector2d<Float> iterationPosition : positions )
        {
            max = Math.max(max, Helper.getAxis(iterationPosition, axis));
        }
        
        return max;
    }
    
    private static ArrayList<RetinaPrimitive> splitDetectorsIntoLines(ArrayList<LineDetectorWithMultiplePoints> lineDetectorsWithMultiplePoints, ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<RetinaPrimitive> result;
        
        result = new ArrayList<>();
        
        for( LineDetectorWithMultiplePoints iterationDetector : lineDetectorsWithMultiplePoints )
        {
            result.addAll(splitDetectorIntoLines(iterationDetector, samples));
        }
        
        return result;
    }
    
    private static ArrayList<RetinaPrimitive> splitDetectorIntoLines(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints, ArrayList<ProcessA.Sample> samples)
    {
        if( lineDetectorWithMultiplePoints.isYAxisSingularity() )
        {
            // handle the special case where its all on one x coordinate
            
            ArrayList<Vector2d<Float>> samplePositions;
            
            samplePositions = new ArrayList<>();
            
            for( int iterationSampleIndex : lineDetectorWithMultiplePoints.integratedSampleIndices )
            {
                Vector2d<Integer> samplePositionAsInt;
                Vector2d<Float> samplePosition;

                samplePositionAsInt = samples.get(iterationSampleIndex).position;
                samplePosition = Datastructures.Vector2d.ConverterHelper.convertIntVectorToFloat(samplePositionAsInt);
                samplePositions.add(samplePosition);
            }
            
            sort(samplePositions, new VectorComperatorByAxis(EnumAxis.Y));
            
            return clusterPointsFromLinedetectorToLinedetectors(samplePositions, EnumAxis.Y);
        }
        else
        {
            ArrayList<Vector2d<Float>> projectedPointPositions;
            
            projectedPointPositions = new ArrayList<>();
            
            // first sort all points after the x position
            // and then "cluster" the lines after the distance between succeeding points
            
            // project
            for( int iterationSampleIndex : lineDetectorWithMultiplePoints.integratedSampleIndices )
            {
                Vector2d<Integer> samplePositionAsInt;
                Vector2d<Float> samplePosition;
                Vector2d<Float> projectedSamplePosition;

                samplePositionAsInt = samples.get(iterationSampleIndex).position;
                samplePosition = new Vector2d<Float>((float)samplePositionAsInt.x, (float)samplePositionAsInt.y);
                projectedSamplePosition = lineDetectorWithMultiplePoints.projectPointOntoLine(samplePosition);

                projectedPointPositions.add(projectedSamplePosition);
            }

            sort(projectedPointPositions, new VectorComperatorByAxis(EnumAxis.X));

            return clusterPointsFromLinedetectorToLinedetectors(projectedPointPositions, EnumAxis.X);
        }
    }
    
    
    private static ArrayList<RetinaPrimitive> clusterPointsFromLinedetectorToLinedetectors(ArrayList<Vector2d<Float>> pointPositions, EnumAxis axis)
    {
        ArrayList<RetinaPrimitive> resultSingleLineDetectors;
        boolean nextIsNewLineStart;
        float lastAxisPosition;
        Vector2d<Float> lineStartPosition;
        Vector2d<Float> lastPoint;
        
        resultSingleLineDetectors = new ArrayList<RetinaPrimitive>();
        
        nextIsNewLineStart = true;

        lineStartPosition = pointPositions.get(0);
        lastAxisPosition = Helper.getAxis(pointPositions.get(0), axis);

        for( Vector2d<Float> iterationPoint : pointPositions )
        {
            if( nextIsNewLineStart )
            {
                lineStartPosition = iterationPoint;
                lastAxisPosition = Helper.getAxis(iterationPoint, axis);

                nextIsNewLineStart = false;

                continue;
            }
            // else we are here

            if( Helper.getAxis(iterationPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE )
            {
                lastAxisPosition = Helper.getAxis(iterationPoint, axis);
            }
            else
            {
                // form a new line
                resultSingleLineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, iterationPoint)));

                nextIsNewLineStart = true;
            }
        }

        // form a new line for the last point
        lastPoint = pointPositions.get(pointPositions.size()-1);

        if( !nextIsNewLineStart && Helper.getAxis(lastPoint, axis) - lastAxisPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE )
        {
            resultSingleLineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(lineStartPosition, lastPoint)));
        }
        
        return resultSingleLineDetectors;
    }
    
    private static void deleteMultiPointDetectorsWhereActiviationIsInsuficient(ArrayList<LineDetectorWithMultiplePoints> lineDetectors)
    {
        int detectorI;
        
        if( !ENABLELOCKING )
        {
            return;
        }
        
        for( detectorI = lineDetectors.size()-1; detectorI >= 0; detectorI-- )
        {
            LineDetectorWithMultiplePoints discardCandidate;
            
            discardCandidate = lineDetectors.get(detectorI);
            
            if( !discardCandidate.isLocked )
            {
                lineDetectors.remove(detectorI);
            }
        }
    }
    
    private static void lockDetectorIfItHasEnoughtActivation(LineDetectorWithMultiplePoints detector)
    {
        detector.isLocked |= detector.getActivation() > Parameters.getProcessdLockingActivation();
    }
    
    private static ArrayList<ProcessA.Sample> filterEndosceletonPoints(ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<ProcessA.Sample> filtered;
        
        filtered = new ArrayList<>();
        
        for( ProcessA.Sample iterationSample : samples )
        {
            if( iterationSample.type == ProcessA.Sample.EnumType.ENDOSCELETON )
            {
                filtered.add(iterationSample);
            }
        }
        
        return filtered;
    }
    
    public Random random = new Random();
    
    private static class VectorComperatorByAxis implements Comparator<Vector2d<Float>>
    {
        
        public VectorComperatorByAxis(EnumAxis axis)
        {
            this.axis = axis;
        }
        
        @Override
        public int compare(Vector2d<Float> a, Vector2d<Float> b) {
            if( Helper.getAxis(a, axis) > Helper.getAxis(b, axis) )
            {
                return 1;
            }
            
            return -1;
        }
        
        private final EnumAxis axis;
    }
    
    private static class RegressionForLineResult
    {
        public float mse;
        
        public float m, n;
    }
    
    private enum EnumAxis
    {
        X,
        Y
    }
}
