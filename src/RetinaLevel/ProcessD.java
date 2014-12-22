package RetinaLevel;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.dot;
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
    }
    
    /**
     * 
     * Invariants:
     *    * a.x < b.x
     * 
     */
    public static class SingleLineDetector
    {
        private SingleLineDetector()
        {
        }
        
        public static SingleLineDetector createFromIntegerPositions(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            SingleLineDetector createdDetector;
            
            createdDetector = new SingleLineDetector();
            createdDetector.aFloat = new Vector2d<Float>((float)a.x, (float)a.y);
            createdDetector.bFloat = new Vector2d<Float>((float)b.x, (float)b.y);
            createdDetector.fullfillABInvariant();
            //createdDetector.integratedSampleIndices = integratedSampleIndices;
            
            // calculate m, n
            // bug outruling
            //createdDetector.m = 0.0f; //(b.y-a.y)/(b.x-a.x);
            //createdDetector.n = 0.0f; //a.y - a.y * createdDetector.m;
            
            return createdDetector;
        }
        
        public static SingleLineDetector createFromFloatPositions(Vector2d<Float> a, Vector2d<Float> b)
        {
            SingleLineDetector createdDetector;
            
            createdDetector = new SingleLineDetector();
            createdDetector.aFloat = a;
            createdDetector.bFloat = b;
            createdDetector.fullfillABInvariant();
            //createdDetector.integratedSampleIndices = integratedSampleIndices;
            
            // calculate m, n
            // bug outruling
            //createdDetector.m = 0.0f; //(b.y-a.y)/(b.x-a.x);
            //createdDetector.n = 0.0f; //a.y - a.y * createdDetector.m;
            
            return createdDetector;
        }
        
        /**
         * 
         * swaps a and b if necessary to fullfil the invariant
         * 
         */
        private void fullfillABInvariant()
        {
            if( aFloat.x > bFloat.x )
            {
                Vector2d<Float> temp;
                
                temp = aFloat;
                aFloat = bFloat;
                bFloat = temp;
            }
        }
        
        
        
        // orginal points, used to determine if a new point can be on the line or not
        public Vector2d<Float> aFloat;
        public Vector2d<Float> bFloat;
        
        public boolean resultOfCombination = false; // for visual debugging, was the detector combined from other detectors?
        
        public boolean isBetweenOrginalStartAndEnd(Vector2d<Float> position) {
            Vector2d<Float> diffAB, diffABnormalizd, diffAPosition;
            float length;
            float dotProduct;
            
            // ASK< maybe the length claculation is unnecessary >
            
            diffAB = new Vector2d<Float>(bFloat.x - aFloat.x, bFloat.y - aFloat.y);
            diffAPosition = new Vector2d<Float>(position.x - aFloat.x, position.y - aFloat.y);
            
            length = (float)Math.sqrt(diffAB.x*diffAB.x + diffAB.y*diffAB.y);
            diffAB.x /= length;
            diffAB.y /= length;
            
            dotProduct = diffAB.x * diffAPosition.x + diffAB.y * diffAPosition.y;
            
            return dotProduct > 0.0f && dotProduct < length;
        }
        
        public Vector2d<Float> getAProjected()
        {
            // TODO< project aFloat onto line defined by m and n >
            return aFloat;
        }
        
        public Vector2d<Float> getBProjected()
        {
            // TODO< project bFloat onto line defined by m and n >
            return bFloat;
        }
        
        public Vector2d<Float> getNormalizedDirection()
        {
            Vector2d<Float> diff;
            
            diff = Vector2d.FloatHelper.sub(getAProjected(), getBProjected());
            return Vector2d.FloatHelper.normalize(diff);
        }
        
        public Vector2d<Float> projectPointOntoLine(Vector2d<Float> point)
        {
            Vector2d<Float> lineDirection;
            Vector2d<Float> diffFromAToPoint;
            float dotResult;

            lineDirection = getNormalizedDirection();
            diffFromAToPoint = sub(point, new Vector2d<>(0.0f, getN()));
            dotResult = dot(lineDirection, diffFromAToPoint);

            return add(new Vector2d<>(0.0f, getN()), getScaled(lineDirection, dotResult));
        }
        
        public float getN()
        {
            // TODO< m = inf special handling >
            return aFloat.y - aFloat.x*getM();
        }
        
        public float getM()
        {
            Vector2d<Float> diff;
            
            diff = sub(bFloat, aFloat);
            return diff.y/diff.x;
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
                // to form a new line detector form a new linedetector by choosing two points at random
                ArrayList<Integer> sampleIndicesForInitialLine = DistinctUtility.getTwoDisjunctNumbers(random, workingSamples.size());

                // create new line detector
                LineDetectorWithMultiplePoints createdLineDetector;

                int sampleIndexA;
                int sampleIndexB;

                sampleIndexA = sampleIndicesForInitialLine.get(0);
                sampleIndexB = sampleIndicesForInitialLine.get(1);

                if( workingSamples.get(sampleIndexA).position.x != workingSamples.get(sampleIndexB).position.x )
                {
                    // TODO< integrate as many as possible points into the detector ? >
                    
                    createdLineDetector = new LineDetectorWithMultiplePoints(sampleIndicesForInitialLine);

                    multiplePointsLineDetector.add(createdLineDetector);
                }
            }
            
            // try to include a random sample into the detectors
            
            int sampleIndex;
            
            sampleIndex = random.nextInt(workingSamples.size());
        
            
            // try to integrate the current sample into line(s)
            tryToIntegratePointIntoAllLineDetectors(sampleIndex, multiplePointsLineDetector, workingSamples);
        }
        
        // delete all detectors for which the activation was not enought
        deleteMultiPointDetectorsWhereActiviationIsInsuficient(multiplePointsLineDetector);
        
        // split the detectors into one or many lines
        ArrayList<SingleLineDetector> resultSingleDetectors = splitDetectorsIntoLines(multiplePointsLineDetector, samples);
        
        return resultSingleDetectors;
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
        
        // first sort all points after the x position
        // TODO< handle the special case where its all on one x coordinate >
        // and then "cluster" the lines after the distance between succeeding points
        
        resultSingleLineDetectors = new ArrayList<>();
        projectedPointPositions = new ArrayList<>();
        
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
        
        sort(projectedPointPositions, new VectorComperatorByX());
        
        // "cluster"

        boolean nextIsNewLineStart;
        float lastXPosition;
        Vector2d<Float> lineStartPosition;
        
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
        
        return resultSingleLineDetectors;
    }
    
    private static void deleteMultiPointDetectorsWhereActiviationIsInsuficient(ArrayList<LineDetectorWithMultiplePoints> lineDetectors)
    {
        int detectorI;
        
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
