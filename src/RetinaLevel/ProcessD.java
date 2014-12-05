package RetinaLevel;

import Datastructures.Vector2d;
import bpsolver.Parameters;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import math.DistinctUtility;

// TODO< lock detectors if they reached a high enought activation >
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
    /**
     * 
     * Invariants:
     *    * a.x < b.x
     * 
     */
    public static class LineDetector
    {
        public static LineDetector createFromIntegerPositions(Vector2d<Integer> a, Vector2d<Integer> b, ArrayList<Integer> integratedSampleIndices)
        {
            LineDetector createdDetector;
            
            createdDetector = new LineDetector();
            createdDetector.aFloat = new Vector2d<Float>((float)a.x, (float)a.y);
            createdDetector.bFloat = new Vector2d<Float>((float)b.x, (float)b.y);
            createdDetector.fullfillABInvariant();
            createdDetector.integratedSampleIndices = integratedSampleIndices;
            
            // calculate m, n
            createdDetector.m = (b.y-a.y)/(b.x-a.x);
            createdDetector.n = a.y - a.y * createdDetector.m;
            
            return createdDetector;
        }
        
        public static LineDetector createFromFloatPositions(Vector2d<Float> a, Vector2d<Float> b, ArrayList<Integer> integratedSampleIndices)
        {
            LineDetector createdDetector;
            
            createdDetector = new LineDetector();
            createdDetector.aFloat = a;
            createdDetector.bFloat = b;
            createdDetector.fullfillABInvariant();
            createdDetector.integratedSampleIndices = integratedSampleIndices;
            
            // calculate m, n
            createdDetector.m = (b.y-a.y)/(b.x-a.x);
            createdDetector.n = a.y - a.y * createdDetector.m;
            
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
        
        public boolean doesContainSampleIndex(int index)
        {
            return integratedSampleIndices.contains(index);
        }
        
        // orginal points, used to determine if a new point can be on the line or not
        public Vector2d<Float> aFloat;
        public Vector2d<Float> bFloat;
        
        public ArrayList<Integer> integratedSampleIndices = new ArrayList<>();
        
        public float m, n;
        
        public float mse = 0.0f;
        
        public boolean isLocked = false; // has the detector received enought activation so it stays?
        
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
        
        public Vector2d<Float> getProjectedNormalizedDirection()
        {
            Vector2d<Float> diff;
            
            diff = Vector2d.FloatHelper.sub(getAProjected(), getBProjected());
            return Vector2d.FloatHelper.normalize(diff);
        }
        
        public float getActivation()
        {
            return (float)integratedSampleIndices.size() + (Parameters.ProcessD.MAXMSE - mse)*Parameters.ProcessD.LOCKINGACTIVATIONMSESCALE;
        }
    }
    
    /**
     * 
     * \param samples doesn't need to be filtered for endo/exosceleton points, it does it itself
     * 
     * \return only the surviving line segments
     */
    public ArrayList<LineDetector> detectLines(ArrayList<ProcessA.Sample> samples)
    {
        ArrayList<ProcessA.Sample> workingSamples;
        ArrayList<LineDetector> resultLineDetectors;
        int sampleI;
        float sumOfAllActivations;
        float maxActivation;
        
        final float SAMPLECOUNTLINEDETECTORMULTIPLIER = 15.0f;
        
        resultLineDetectors = new ArrayList<>();
        sumOfAllActivations = 0.0f;
        maxActivation = 0.0f;
        
        workingSamples = filterEndosceletonPoints(samples);
        
        int lineDetectorCounter;
        
        for( lineDetectorCounter = 0; lineDetectorCounter < Math.round((float)samples.size()*SAMPLECOUNTLINEDETECTORMULTIPLIER); lineDetectorCounter++ )
        {
            // to form a new line detector form a new linedetector by choosing two points at random
            ArrayList<Integer> sampleIndicesForInitialLine = DistinctUtility.getTwoDisjunctNumbers(random, workingSamples.size());

            // create new line detector
            LineDetector createdLineDetector;

            int sampleIndexA;
            int sampleIndexB;

            sampleIndexA = sampleIndicesForInitialLine.get(0);
            sampleIndexB = sampleIndicesForInitialLine.get(1);

            if( workingSamples.get(sampleIndexA).position.x != workingSamples.get(sampleIndexB).position.x )
            {
                createdLineDetector = LineDetector.createFromIntegerPositions(workingSamples.get(sampleIndexA).position, workingSamples.get(sampleIndexB).position, sampleIndicesForInitialLine);

                resultLineDetectors.add(createdLineDetector);
                sumOfAllActivations += createdLineDetector.getActivation();

                maxActivation = calculationMaxActivation(resultLineDetectors);
            }
        }
        
        
        for( sampleI = 0; sampleI < workingSamples.size(); sampleI++ )
        {
            // TODO< refactor into 3 functions >
            
            
            /*
            for( lineDetectorCounter = 0; lineDetectorCounter < 5; lineDetectorCounter++ )
            {
                // to form a new line detector form a new linedetector by choosing two points at random
                ArrayList<Integer> sampleIndicesForInitialLine = DistinctUtility.getTwoDisjunctNumbers(random, workingSamples.size());

                // create new line detector
                LineDetector createdLineDetector;

                int sampleIndexA;
                int sampleIndexB;

                sampleIndexA = sampleIndicesForInitialLine.get(0);
                sampleIndexB = sampleIndicesForInitialLine.get(1);
                
                if( workingSamples.get(sampleIndexA).position.x != workingSamples.get(sampleIndexB).position.x )
                {
                    createdLineDetector = LineDetector.createFromIntegerPositions(workingSamples.get(sampleIndexA).position, workingSamples.get(sampleIndexB).position, sampleIndicesForInitialLine);
                    
                    resultLineDetectors.add(createdLineDetector);
                    sumOfAllActivations += createdLineDetector.getActivation();
                    
                    maxActivation = calculationMaxActivation(resultLineDetectors);
                }
            }
            */
            
            
            // try to integrate the current sample into line(s)
            for( LineDetector iteratorDetector : resultLineDetectors )
            {
                ProcessA.Sample currentSample;
                float mse;
                int sampleIndexI;
                
                currentSample = workingSamples.get(sampleI);
                
                if( iteratorDetector.doesContainSampleIndex(sampleI) )
                {
                    continue;
                }
                // else we are here
                
                if( !iteratorDetector.isBetweenOrginalStartAndEnd(convertVectorToFloat(currentSample.position)) )
                {
                    continue;
                }
                // else we are here
                
                
                SimpleRegression regression = new SimpleRegression();
                for (sampleIndexI = 0; sampleIndexI < iteratorDetector.integratedSampleIndices.size(); sampleIndexI++)
                {
                    int sampleIndex;
                    
                    
                    sampleIndex = iteratorDetector.integratedSampleIndices.get(sampleIndexI);
                    currentSample = workingSamples.get(sampleIndex);
                    
                    regression.addData((float)currentSample.position.y, (float)currentSample.position.x);
                }
                
                
                regression.addData(currentSample.position.y, currentSample.position.x);
                
                mse = (float)regression.getMeanSquareError();
                
                if( mse < Parameters.ProcessD.MAXMSE )
                {
                    // we do this to save the time for summing up all activations of all detectors after this modification
                    sumOfAllActivations -= iteratorDetector.getActivation();
                    
                    iteratorDetector.mse = mse;
                    
                    iteratorDetector.integratedSampleIndices.add(sampleI);
                    
                    iteratorDetector.n = (float)regression.getIntercept();
                    iteratorDetector.m = (float)regression.getSlope();
                    
                    lockDetectorIfItHasEnoughtActivation(iteratorDetector);
                    
                    // see above
                    sumOfAllActivations += iteratorDetector.getActivation();
                    
                    maxActivation = calculationMaxActivation(resultLineDetectors);
                }
            }
            
            
            // try to delete (random) detectors if its activation is below the threashold and it didn't got locked
            /*
            int counter;
            
            for( counter = 0; counter < 2; counter++ )
            {
                int discardCandidateDetectorIndex;
                LineDetector discardCandidate;

                discardCandidateDetectorIndex = random.nextInt(resultLineDetectors.size());
                discardCandidate = resultLineDetectors.get(discardCandidateDetectorIndex);

                if( !discardCandidate.isLocked && discardCandidate.getActivation() < maxActivation*Parameters.ProcessD.MINIMALACTIVATIONTOSUMRATIO )
                {
                    maxActivation = calculationMaxActivation(resultLineDetectors);
                    sumOfAllActivations -= discardCandidate.getActivation();
                    
                    resultLineDetectors.remove(discardCandidateDetectorIndex);
                }
            }*/
            
        }
        
        
        // delete all detectors for which the activation was not enought
        int detectorI;
        
        for( detectorI = resultLineDetectors.size()-1; detectorI >= 0; detectorI-- )
        {
            LineDetector discardCandidate;
            
            discardCandidate = resultLineDetectors.get(detectorI);
            
            if( !discardCandidate.isLocked )
            {
                resultLineDetectors.remove(detectorI);
            }
        }
        
        return resultLineDetectors;
    }
    
    private static float calculationMaxActivation(ArrayList<LineDetector> lineDetectors)
    {
        float maxActivation;
        
        maxActivation = 0.0f;
        
        for( LineDetector iterator : lineDetectors )
        {
            maxActivation = Math.max(iterator.getActivation(), maxActivation);
        }
        
        return maxActivation;
    }
    
    private static void lockDetectorIfItHasEnoughtActivation(LineDetector detector)
    {
        detector.isLocked |= detector.getActivation() > Parameters.ProcessD.LOCKINGACTIVATION;
        
        if( detector.isLocked )
        {
            int x = 0;
        }
    }
    
    private static Vector2d<Float> convertVectorToFloat(Vector2d<Integer> vector)
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
    
    
}
