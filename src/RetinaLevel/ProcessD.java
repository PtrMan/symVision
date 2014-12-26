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
            return Vector2d.FloatHelper.normalize(new Vector2d<>(1.0f, m));
        }
        
        // TODO< just simply test flag >
        public boolean isYAxisSingularity()
        {
            return Float.isInfinite(m);
        }
    }
    
    /**
     * 
     * \param samples doesn't need to be filtered for endo/exosceleton points, it does it itself
     * 
     * \return only the surviving line segments
     */
    public ArrayList<SingleLineDetector> detectLines(ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<ProcessA.Sample> workingSamples;
        ArrayList<LineDetectorWithMultiplePoints> multiplePointsLineDetector;
        
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
        ArrayList<SingleLineDetector> resultSingleDetectors = splitDetectorsIntoLines(multiplePointsLineDetector, samples);
        
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
        Vector2d<Integer> centerPositionAsInt;
        Vector2d<Float> centerPosition;
        int counter;
        
        centerPositionAsInt = workingSamples.get(centerPointIndex).position;
        centerPosition = convertVectorFromIntToFloat(centerPositionAsInt);
        
        for( counter = 0; counter < count; )
        {
            int chosenPointIndex;
            ArrayList<Integer> chosenPointIndexAsList;
            Vector2d<Integer> chosenPointPositionAsInt;
            Vector2d<Float> chosenPointPosition;
            
            chosenPointIndexAsList = DistinctUtility.getDisjuctNumbersTo(random, alreadyIntegratedPointIndices, 1, workingSamples.size());
            Assert.Assert(chosenPointIndexAsList.size() == 1, "");
            chosenPointIndex = chosenPointIndexAsList.get(0);
            
            // check if it is inside radius
            chosenPointPositionAsInt = workingSamples.get(chosenPointIndex).position;
            chosenPointPosition = convertVectorFromIntToFloat(chosenPointPositionAsInt);
            
            if( Helper.isDistanceBetweenPositionsBelow(centerPosition, chosenPointPosition, HardParameters.ProcessD.EARLYCANDIDATEMAXDISTANCE) )
            {
                alreadyIntegratedPointIndices.add(chosenPointIndex);
                
                // we have one more point, so increment counter
                counter++;
                continue;
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
            positionsOfSamples.add(convertVectorFromIntToFloat(currentSample.position));
            
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
            convertedPosition = convertVectorFromIntToFloat(currentSample.position);
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
            
            if( axis == EnumAxis.X )
            {
                dimensionCounterIndex = Math.round(iterationPosition.x);
            }
            else
            {
                dimensionCounterIndex = Math.round(iterationPosition.y);
            }
            
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
            if( axis == EnumAxis.X )
            {
                max = Math.max(max, iterationPosition.x);
            }
            else
            {
                max = Math.max(max, iterationPosition.y);
            }
        }
        
        return max;
    }
    
    private static ArrayList<SingleLineDetector> splitDetectorsIntoLines(ArrayList<LineDetectorWithMultiplePoints> lineDetectorsWithMultiplePoints, ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<SingleLineDetector> result;
        
        result = new ArrayList<>();
        
        for( LineDetectorWithMultiplePoints iterationDetector : lineDetectorsWithMultiplePoints )
        {
            result.addAll(splitDetectorIntoLines(iterationDetector, samples));
        }
        
        return result;
    }
    
    private static ArrayList<SingleLineDetector> splitDetectorIntoLines(LineDetectorWithMultiplePoints lineDetectorWithMultiplePoints, ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<SingleLineDetector> resultSingleLineDetectors;
        ArrayList<Vector2d<Float>> projectedPointPositions;
        
        resultSingleLineDetectors = new ArrayList<>();
        projectedPointPositions = new ArrayList<>();
        
        if( lineDetectorWithMultiplePoints.isYAxisSingularity() )
        {
            // TODO< handle the special case where its all on one x coordinate >
        

            // TODO TODO TODO
        }
        else
        {
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

            if( Float.isNaN(projectedPointPositions.get(0).x) )
            {
                int z = 1;
            }

            sort(projectedPointPositions, new VectorComperatorByX());

            if( Float.isNaN(projectedPointPositions.get(0).x) )
            {
                int z = 1;
            }

            // "cluster"

            boolean nextIsNewLineStart;
            float lastXPosition;
            Vector2d<Float> lineStartPosition;
            Vector2d<Float> lastPoint;
            
            nextIsNewLineStart = true;

            lineStartPosition = projectedPointPositions.get(0);
            lastXPosition = projectedPointPositions.get(0).x;


            for( Vector2d<Float> iterationPoint : projectedPointPositions )
            {
                if( nextIsNewLineStart )
                {
                    lineStartPosition = iterationPoint;
                    lastXPosition = iterationPoint.x;

                    nextIsNewLineStart = false;

                    continue;
                }
                // else we are here

                if( iterationPoint.x - lastXPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE )
                {
                    lastXPosition = iterationPoint.x;
                }
                else
                {
                    // form a new line
                    resultSingleLineDetectors.add(SingleLineDetector.createFromFloatPositions(lineStartPosition, iterationPoint));

                    nextIsNewLineStart = true;
                }
            }
            
            // form a new line for the last point
            lastPoint = projectedPointPositions.get(projectedPointPositions.size()-1);
            
            if( !nextIsNewLineStart && lastPoint.x - lastXPosition < HardParameters.ProcessD.LINECLUSTERINGMAXDISTANCE )
            {
                resultSingleLineDetectors.add(SingleLineDetector.createFromFloatPositions(lineStartPosition, lastPoint));
            }
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
    
    private static Vector2d<Float> convertVectorFromIntToFloat(Vector2d<Integer> vector)
    {
        return new Vector2d<Float>((float)vector.x, (float)vector.y);
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
    
    private static class VectorComperatorByX implements Comparator<Vector2d<Float>>
    {

        @Override
        public int compare(Vector2d<Float> a, Vector2d<Float> b) {
            if( a.x > b.x )
            {
                return 1;
            }
            
            return -1;
        }
        
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
