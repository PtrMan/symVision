package RetinaLevel;

import Datastructures.Vector2d;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import math.DistinctUtility;

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
        
        resultLineDetectors = new ArrayList<>();
        
        workingSamples = filterEndosceletonPoints(samples);
        
        for( sampleI = 0; sampleI < workingSamples.size(); sampleI++ )
        {
            // to form a new line detector form a new linedetector by choosing two points at random
            ArrayList<Integer> sampleIndicesForInitialLine = DistinctUtility.getTwoDisjunctNumbers(random, workingSamples.size());

            // create new line detector
            LineDetector createdLineDetector;

            int sampleIndexA;
            int sampleIndexB;

            sampleIndexA = sampleIndicesForInitialLine.get(0);
            sampleIndexB = sampleIndicesForInitialLine.get(1);

            createdLineDetector = LineDetector.createFromIntegerPositions(workingSamples.get(sampleIndexA).position, workingSamples.get(sampleIndexB).position, sampleIndicesForInitialLine);

            resultLineDetectors.add(createdLineDetector);
            
            
            
            // try to integrate the current sample into line(s)
            for( LineDetector iteratorDetector : resultLineDetectors )
            {
                ProcessA.Sample currentSample;
                
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
                
                
                int sampleIndexI;
                
                
                SimpleRegression regression = new SimpleRegression();
                for (sampleIndexI = 0; sampleIndexI < iteratorDetector.integratedSampleIndices.size(); sampleIndexI++)
                {
                    int sampleIndex;
                    
                    
                    sampleIndex = iteratorDetector.integratedSampleIndices.get(sampleIndexI);
                    currentSample = workingSamples.get(sampleIndex);
                    
                    regression.addData((float)currentSample.position.y, (float)currentSample.position.x);
                }
                
                
                regression.addData(currentSample.position.y, currentSample.position.x);
                
                float mse = (float)regression.getMeanSquareError();
                
                if (mse < MAXMSE)
                {
                    iteratorDetector.integratedSampleIndices.add(sampleI);
                    
                    iteratorDetector.n = (float)regression.getIntercept();
                    iteratorDetector.m = (float)regression.getSlope();
                }
            }
        }
        
        return resultLineDetectors;
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
    
    private final static float MAXMSE = 4.0f; // max mean square error for inclusion of a point
}
